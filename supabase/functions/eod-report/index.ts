// Supabase Edge Function: Automated End of Day (EOD) Report Generator & Dispatcher
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    const { company_id, target_date } = await req.json().catch(() => ({}));
    const dateStr = target_date || new Date().toISOString().split("T")[0];

    // Find all reps in the company or active today
    let query = supabase.from("profiles").select("id, company_id, full_name, email, manager_id").eq("role", "rep");
    if (company_id) query = query.eq("company_id", company_id);
    const { data: reps } = await query;

    const generatedReports: any[] = [];

    for (const rep of reps || []) {
      // 1. Calculate visits for today
      const { data: visits } = await supabase
        .from("visits")
        .select("id, status, distance_to_client_meters, check_in_time, clients(name, client_type)")
        .eq("rep_id", rep.id)
        .gte("check_in_time", `${dateStr}T00:00:00Z`)
        .lte("check_in_time", `${dateStr}T23:59:59Z`);

      const totalVisits = visits?.length || 0;
      const verifiedVisits = visits?.filter((v) => v.status === "done").length || 0;
      const flaggedVisits = visits?.filter((v) => v.status === "needs_review").length || 0;

      // 2. Calculate day session & distance
      const { data: session } = await supabase
        .from("day_sessions")
        .select("total_distance_meters, started_at, ended_at")
        .eq("rep_id", rep.id)
        .eq("session_date", dateStr)
        .maybeSingle();

      const distanceKm = Number(((session?.total_distance_meters || 0) / 1000).toFixed(2));

      // 3. Any security flags
      const { data: flags } = await supabase
        .from("flags")
        .select("flag_type, severity, message, created_at")
        .eq("rep_id", rep.id)
        .gte("created_at", `${dateStr}T00:00:00Z`);

      const reportPayload = {
        rep_id: rep.id,
        company_id: rep.company_id,
        report_date: dateStr,
        total_visits: totalVisits,
        verified_visits: verifiedVisits,
        flagged_visits: flaggedVisits,
        total_distance_km: distanceKm,
        summary_json: {
          rep_name: rep.full_name,
          started_at: session?.started_at,
          ended_at: session?.ended_at,
          visits_detail: visits,
          flags: flags || [],
        },
        emailed_to: [rep.email],
      };

      const { data: savedReport } = await supabase.from("reports").insert(reportPayload).select().single();
      generatedReports.push(savedReport);
    }

    return new Response(JSON.stringify({ success: true, reports_count: generatedReports.length }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
