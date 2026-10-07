// Supabase Edge Function: GPS Verified Check-In Engine (TypeScript / Deno)
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

interface CheckInPayload {
  client_id: string;
  plan_item_id?: string;
  day_session_id?: string;
  latitude: number;
  longitude: number;
  accuracy: number;
  is_mock: boolean;
  is_rooted_signal?: boolean;
  timestamp: string;
  notes?: string;
  photo_storage_path?: string;
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "Missing Authorization header" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseServiceKey, {
      auth: { persistSession: false },
    });

    // Authenticate user token
    const token = authHeader.replace("Bearer ", "");
    const { data: { user }, error: userError } = await supabase.auth.getUser(token);
    if (userError || !user) {
      return new Response(JSON.stringify({ error: "Unauthorized user" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const body: CheckInPayload = await req.json();
    const { client_id, plan_item_id, day_session_id, latitude, longitude, accuracy, is_mock, is_rooted_signal, notes, photo_storage_path } = body;

    // 1. Fetch user profile and company settings
    const { data: profile, error: profileError } = await supabase
      .from("profiles")
      .select("company_id, role")
      .eq("id", user.id)
      .single();

    if (profileError || !profile) {
      return new Response(JSON.stringify({ error: "Profile not found" }), {
        status: 404,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const { data: company } = await supabase
      .from("companies")
      .select("check_in_radius_meters")
      .eq("id", profile.company_id)
      .single();

    const allowedRadius = company?.check_in_radius_meters ?? 50;

    // 2. Fetch Client coordinates & calculate exact PostGIS distance
    const { data: client, error: clientError } = await supabase
      .from("clients")
      .select("id, name, latitude, longitude")
      .eq("id", client_id)
      .eq("company_id", profile.company_id)
      .single();

    if (clientError || !client) {
      return new Response(JSON.stringify({ error: "Client not found or unassociated" }), {
        status: 404,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Great circle distance in meters (Haversine / PostGIS equivalent)
    const R = 6371000; // meters
    const dLat = ((client.latitude - latitude) * Math.PI) / 180;
    const dLon = ((client.longitude - longitude) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((latitude * Math.PI) / 180) *
        Math.cos((client.latitude * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distanceMeters = Math.round(R * c);

    // 3. Security checks: Check for Mock GPS or Rooted signals
    const flagsToRecord: any[] = [];
    if (is_mock) {
      flagsToRecord.push({
        company_id: profile.company_id,
        rep_id: user.id,
        flag_type: "mock_gps",
        severity: "critical",
        message: `Rep attempted check-in at ${client.name} using mock location provider.`,
        metadata: { client_id, latitude, longitude, accuracy },
      });
    }

    if (is_rooted_signal) {
      flagsToRecord.push({
        company_id: profile.company_id,
        rep_id: user.id,
        flag_type: "rooted_device",
        severity: "high",
        message: `Device root or signature tampering detected during check-in.`,
        metadata: { client_id },
      });
    }

    // Fetch previous location point to calculate speed jump
    const { data: lastPoint } = await supabase
      .from("location_points")
      .select("latitude, longitude, recorded_at")
      .eq("rep_id", user.id)
      .order("recorded_at", { ascending: false })
      .limit(1)
      .maybeSingle();

    if (lastPoint) {
      const prevTime = new Date(lastPoint.recorded_at).getTime();
      const currTime = new Date().getTime();
      const elapsedSeconds = Math.max(1, (currTime - prevTime) / 1000);
      
      const pDLat = ((lastPoint.latitude - latitude) * Math.PI) / 180;
      const pDLon = ((lastPoint.longitude - longitude) * Math.PI) / 180;
      const pA = Math.sin(pDLat / 2) * Math.sin(pDLat / 2) +
        Math.cos((latitude * Math.PI) / 180) * Math.cos((lastPoint.latitude * Math.PI) / 180) *
        Math.sin(pDLon / 2) * Math.sin(pDLon / 2);
      const pDist = R * 2 * Math.atan2(Math.sqrt(pA), Math.sqrt(1 - pA));
      const speedKmH = (pDist / elapsedSeconds) * 3.6;

      if (speedKmH > 150) {
        flagsToRecord.push({
          company_id: profile.company_id,
          rep_id: user.id,
          flag_type: "impossible_speed",
          severity: "high",
          message: `Impossible velocity jump: ${Math.round(speedKmH)} km/h detected before check-in.`,
          metadata: { speedKmH, jumpDistanceMeters: Math.round(pDist) },
        });
      }
    }

    // 4. Verification Logic:
    // Rule: if (distance minus accuracy) <= company radius then verified
    const effectiveDistance = Math.max(0, distanceMeters - accuracy);
    let visitStatus: "done" | "needs_review" = "done";
    let isRejected = false;
    let rejectionReason = "";

    if (effectiveDistance > allowedRadius) {
      isRejected = true;
      rejectionReason = `Too far from client: current distance is ${distanceMeters}m (Allowed radius: ${allowedRadius}m).`;
    } else if (accuracy > 50) {
      // High accuracy drift requires timestamped photo proof
      visitStatus = "needs_review";
      if (!photo_storage_path) {
        return new Response(
          JSON.stringify({
            verified: false,
            needs_photo: true,
            message: "GPS accuracy exceeds 50m. A photo proof is required to verify this check-in.",
            distanceMeters,
            accuracy,
          }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    }

    if (isRejected) {
      return new Response(
        JSON.stringify({
          verified: false,
          rejected: true,
          message: rejectionReason,
          distanceMeters,
          allowedRadius,
        }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Insert any detected security flags
    if (flagsToRecord.length > 0) {
      await supabase.from("flags").insert(flagsToRecord);
    }

    // Signed photo URL if provided
    let signedPhotoUrl = null;
    if (photo_storage_path) {
      const { data: signedData } = await supabase.storage
        .from("visit_photos")
        .createSignedUrl(photo_storage_path, 3600); // 1-hour expiry
      signedPhotoUrl = signedData?.signedUrl;
    }

    // 5. Record Visit in Database
    const { data: visitRecord, error: visitError } = await supabase
      .from("visits")
      .insert({
        company_id: profile.company_id,
        rep_id: user.id,
        client_id,
        plan_item_id: plan_item_id || null,
        day_session_id: day_session_id || null,
        check_in_latitude: latitude,
        check_in_longitude: longitude,
        check_in_location: `POINT(${longitude} ${latitude})`,
        gps_accuracy_meters: accuracy,
        distance_to_client_meters: distanceMeters,
        is_mock_location: is_mock,
        status: visitStatus,
        notes,
        photo_storage_path,
        photo_signed_url: signedPhotoUrl,
      })
      .select()
      .single();

    if (visitError) {
      return new Response(JSON.stringify({ error: visitError.message }), {
        status: 500,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // If plan_item_id is linked, update its status
    if (plan_item_id) {
      await supabase
        .from("plan_items")
        .update({
          status: visitStatus,
          completed_at: new Date().toISOString(),
        })
        .eq("id", plan_item_id);
    }

    return new Response(
      JSON.stringify({
        verified: true,
        status: visitStatus,
        distanceMeters,
        allowedRadius,
        visit: visitRecord,
        message: visitStatus === "done" ? "Check-in verified successfully!" : "Check-in submitted for manager review.",
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
