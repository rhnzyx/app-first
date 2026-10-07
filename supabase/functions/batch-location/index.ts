// Supabase Edge Function: Batch Location Breadcrumbs Ingestion & Live Position Syncer
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

interface LocationPointDto {
  latitude: number;
  longitude: number;
  accuracy: number;
  altitude?: number;
  speed?: number;
  bearing?: number;
  is_mock?: boolean;
  recorded_at: string;
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

    const token = authHeader.replace("Bearer ", "");
    const { data: { user }, error: userError } = await supabase.auth.getUser(token);
    if (userError || !user) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const { points, day_session_id }: { points: LocationPointDto[]; day_session_id?: string } = await req.json();

    if (!Array.isArray(points) || points.length === 0) {
      return new Response(JSON.stringify({ success: true, inserted: 0 }), {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const { data: profile } = await supabase
      .from("profiles")
      .select("company_id")
      .eq("id", user.id)
      .single();

    if (!profile) {
      return new Response(JSON.stringify({ error: "Profile not found" }), {
        status: 404,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Prepare batch rows
    const rows = points.map((p) => ({
      company_id: profile.company_id,
      rep_id: user.id,
      day_session_id: day_session_id || null,
      point: `POINT(${p.longitude} ${p.latitude})`,
      latitude: p.latitude,
      longitude: p.longitude,
      accuracy: p.accuracy,
      altitude: p.altitude ?? null,
      speed: p.speed ?? 0,
      bearing: p.bearing ?? null,
      is_mock: p.is_mock ?? false,
      recorded_at: p.recorded_at,
    }));

    await supabase.from("location_points").insert(rows);

    // Update live_positions with the most recent point
    const latest = points[points.length - 1];
    const isMoving = (latest.speed ?? 0) > 1.5;
    await supabase.from("live_positions").upsert({
      rep_id: user.id,
      company_id: profile.company_id,
      point: `POINT(${latest.longitude} ${latest.latitude})`,
      latitude: latest.latitude,
      longitude: latest.longitude,
      accuracy: latest.accuracy,
      speed: latest.speed ?? 0,
      status: isMoving ? "active" : "idle",
      last_updated: new Date().toISOString(),
    });

    return new Response(JSON.stringify({ success: true, count: rows.length }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
