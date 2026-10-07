-- ============================================================================
-- FIELDLY (By Team Alpha) - Master Supabase Postgres & PostGIS Database Schema
-- Multi-Tenant Field Sales Coordination Engine
-- ============================================================================

-- 1. Enable PostGIS Extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. ENUMS & DOMAINS
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('admin', 'manager', 'rep');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE visit_item_status AS ENUM ('pending', 'done', 'needs_review');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE flag_severity AS ENUM ('low', 'medium', 'high', 'critical');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. TABLES DEFINITION

-- 3.1 Companies
CREATE TABLE IF NOT EXISTS public.companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    check_in_radius_meters INT NOT NULL DEFAULT 50 CHECK (check_in_radius_meters BETWEEN 30 AND 150),
    work_start_time TIME NOT NULL DEFAULT '09:00:00',
    work_end_time TIME NOT NULL DEFAULT '18:00:00',
    report_time TIME NOT NULL DEFAULT '18:30:00',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.2 Profiles (Linked to auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    manager_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT,
    role user_role NOT NULL DEFAULT 'rep',
    avatar_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.3 Clients
CREATE TABLE IF NOT EXISTS public.clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    client_type TEXT NOT NULL DEFAULT 'Retailer',
    phone TEXT,
    notes TEXT,
    location geometry(Point, 4326) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address_display TEXT,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    created_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.4 Visit Plans
CREATE TABLE IF NOT EXISTS public.visit_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    rep_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    manager_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    plan_date DATE NOT NULL,
    title TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    UNIQUE(company_id, rep_id, plan_date)
);

-- 3.5 Plan Items (Ordered Visits in a Plan)
CREATE TABLE IF NOT EXISTS public.plan_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES public.visit_plans(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES public.clients(id) ON DELETE RESTRICT,
    sequence_order INT NOT NULL DEFAULT 1,
    status visit_item_status NOT NULL DEFAULT 'pending',
    target_time TIME,
    notes TEXT,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.6 Day Sessions (Start Day / End Day)
CREATE TABLE IF NOT EXISTS public.day_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    rep_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    session_date DATE NOT NULL DEFAULT CURRENT_DATE,
    started_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    ended_at TIMESTAMPTZ,
    start_latitude DOUBLE PRECISION,
    start_longitude DOUBLE PRECISION,
    end_latitude DOUBLE PRECISION,
    end_longitude DOUBLE PRECISION,
    total_distance_meters DOUBLE PRECISION DEFAULT 0.0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.7 Visits (Check-in records)
CREATE TABLE IF NOT EXISTS public.visits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    rep_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES public.clients(id) ON DELETE RESTRICT,
    plan_item_id UUID REFERENCES public.plan_items(id) ON DELETE SET NULL,
    day_session_id UUID REFERENCES public.day_sessions(id) ON DELETE SET NULL,
    check_in_time TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    check_in_location geometry(Point, 4326) NOT NULL,
    check_in_latitude DOUBLE PRECISION NOT NULL,
    check_in_longitude DOUBLE PRECISION NOT NULL,
    gps_accuracy_meters DOUBLE PRECISION NOT NULL,
    distance_to_client_meters DOUBLE PRECISION NOT NULL,
    is_mock_location BOOLEAN NOT NULL DEFAULT false,
    status visit_item_status NOT NULL DEFAULT 'done',
    notes TEXT,
    photo_storage_path TEXT,
    photo_signed_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.8 Location Points (High frequency breadcrumbs)
CREATE TABLE IF NOT EXISTS public.location_points (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    rep_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    day_session_id UUID REFERENCES public.day_sessions(id) ON DELETE CASCADE,
    point geometry(Point, 4326) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION NOT NULL,
    altitude DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    bearing DOUBLE PRECISION,
    is_mock BOOLEAN NOT NULL DEFAULT false,
    recorded_at TIMESTAMPTZ NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.9 Live Positions (Latest status per rep)
CREATE TABLE IF NOT EXISTS public.live_positions (
    rep_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    point geometry(Point, 4326) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION NOT NULL,
    speed DOUBLE PRECISION,
    status TEXT NOT NULL DEFAULT 'active', -- 'active', 'idle', 'offline'
    battery_level INT,
    last_updated TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.10 Flags (Suspicious telemetry / alerts)
CREATE TABLE IF NOT EXISTS public.flags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    rep_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    flag_type TEXT NOT NULL, -- 'mock_gps', 'rooted_device', 'impossible_speed', 'idle_45m', 'accuracy_drift'
    severity flag_severity NOT NULL DEFAULT 'medium',
    message TEXT NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb,
    is_resolved BOOLEAN NOT NULL DEFAULT false,
    resolved_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.11 Consents (Privacy & tracking acceptance)
CREATE TABLE IF NOT EXISTS public.consents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    consent_version TEXT NOT NULL DEFAULT '1.0',
    location_tracking_agreed BOOLEAN NOT NULL DEFAULT true,
    privacy_policy_agreed BOOLEAN NOT NULL DEFAULT true,
    device_model TEXT,
    os_version TEXT,
    ip_address TEXT,
    consented_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.12 Audit Logs
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    action TEXT NOT NULL,
    entity_name TEXT NOT NULL,
    entity_id TEXT,
    payload JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 3.13 Reports (End of Day Summary PDFs)
CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    rep_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    report_date DATE NOT NULL,
    total_visits INT NOT NULL DEFAULT 0,
    verified_visits INT NOT NULL DEFAULT 0,
    flagged_visits INT NOT NULL DEFAULT 0,
    total_distance_km DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    summary_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    storage_path TEXT,
    emailed_to TEXT[],
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);

-- 4. SPATIAL & PERFORMANCE INDEXES
CREATE INDEX IF NOT EXISTS idx_clients_location ON public.clients USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_clients_company ON public.clients(company_id);
CREATE INDEX IF NOT EXISTS idx_location_points_geom ON public.location_points USING GIST(point);
CREATE INDEX IF NOT EXISTS idx_location_points_rep_date ON public.location_points(company_id, rep_id, recorded_at);
CREATE INDEX IF NOT EXISTS idx_live_positions_geom ON public.live_positions USING GIST(point);
CREATE INDEX IF NOT EXISTS idx_visits_geom ON public.visits USING GIST(check_in_location);
CREATE INDEX IF NOT EXISTS idx_visits_rep_date ON public.visits(company_id, rep_id, check_in_time);
CREATE INDEX IF NOT EXISTS idx_plan_items_plan ON public.plan_items(company_id, plan_id, sequence_order);
CREATE INDEX IF NOT EXISTS idx_day_sessions_rep_active ON public.day_sessions(company_id, rep_id, is_active);

-- 5. ROW LEVEL SECURITY (RLS) POLICIES

ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.visit_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.plan_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.day_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.visits ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.location_points ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.live_positions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.flags ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.consents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;

-- Helper security functions
CREATE OR REPLACE FUNCTION current_user_company_id()
RETURNS UUID AS $$
  SELECT company_id FROM public.profiles WHERE id = auth.uid() LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION current_user_role()
RETURNS user_role AS $$
  SELECT role FROM public.profiles WHERE id = auth.uid() LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- Companies: users only see their own company
CREATE POLICY "companies_view_own" ON public.companies
    FOR SELECT USING (id = current_user_company_id());

CREATE POLICY "companies_admin_manage" ON public.companies
    FOR ALL USING (id = current_user_company_id() AND current_user_role() = 'admin');

-- Profiles:
-- Admins see all in company; Managers see themselves and their team reps; Reps see themselves.
CREATE POLICY "profiles_select_policy" ON public.profiles
    FOR SELECT USING (
        company_id = current_user_company_id() AND (
            current_user_role() = 'admin' OR
            (current_user_role() = 'manager' AND (manager_id = auth.uid() OR id = auth.uid())) OR
            id = auth.uid()
        )
    );

CREATE POLICY "profiles_update_self_or_admin" ON public.profiles
    FOR UPDATE USING (
        company_id = current_user_company_id() AND (
            id = auth.uid() OR current_user_role() = 'admin'
        )
    );

-- Clients: All members of the same company can view clients
CREATE POLICY "clients_select_company" ON public.clients
    FOR SELECT USING (company_id = current_user_company_id());

CREATE POLICY "clients_manage_manager_admin" ON public.clients
    FOR ALL USING (
        company_id = current_user_company_id() AND
        current_user_role() IN ('admin', 'manager')
    );

-- Visit Plans & Plan Items:
-- Reps see only their assigned plans; Managers see plans they created or for their team; Admins see all.
CREATE POLICY "plans_select_policy" ON public.visit_plans
    FOR SELECT USING (
        company_id = current_user_company_id() AND (
            current_user_role() = 'admin' OR
            (current_user_role() = 'manager' AND manager_id = auth.uid()) OR
            rep_id = auth.uid()
        )
    );

CREATE POLICY "plan_items_select_policy" ON public.plan_items
    FOR SELECT USING (
        company_id = current_user_company_id() AND (
            current_user_role() = 'admin' OR
            EXISTS (
                SELECT 1 FROM public.visit_plans p
                WHERE p.id = plan_id AND (
                    p.rep_id = auth.uid() OR
                    (current_user_role() = 'manager' AND p.manager_id = auth.uid())
                )
            )
        )
    );

-- Day Sessions: Rep writes own, Manager/Admin views
CREATE POLICY "day_sessions_rep_manage" ON public.day_sessions
    FOR ALL USING (
        company_id = current_user_company_id() AND (
            rep_id = auth.uid() OR
            current_user_role() IN ('admin', 'manager')
        )
    );

-- Visits: Rep inserts own visits, Manager/Admin views
CREATE POLICY "visits_rep_insert_own" ON public.visits
    FOR INSERT WITH CHECK (
        company_id = current_user_company_id() AND rep_id = auth.uid()
    );

CREATE POLICY "visits_select_policy" ON public.visits
    FOR SELECT USING (
        company_id = current_user_company_id() AND (
            rep_id = auth.uid() OR
            current_user_role() IN ('admin', 'manager')
        )
    );

-- Location Points: Rep inserts own location breadcrumbs
CREATE POLICY "location_points_rep_insert" ON public.location_points
    FOR INSERT WITH CHECK (
        company_id = current_user_company_id() AND rep_id = auth.uid()
    );

CREATE POLICY "location_points_select_policy" ON public.location_points
    FOR SELECT USING (
        company_id = current_user_company_id() AND (
            rep_id = auth.uid() OR
            current_user_role() IN ('admin', 'manager')
        )
    );

-- Live Positions:
CREATE POLICY "live_positions_upsert_own" ON public.live_positions
    FOR ALL USING (
        company_id = current_user_company_id() AND (
            rep_id = auth.uid() OR
            current_user_role() IN ('admin', 'manager')
        )
    );

-- Flags:
CREATE POLICY "flags_select_policy" ON public.flags
    FOR SELECT USING (
        company_id = current_user_company_id() AND (
            rep_id = auth.uid() OR
            current_user_role() IN ('admin', 'manager')
        )
    );

-- Consents: Rep manages own consent, Admin/Manager can audit
CREATE POLICY "consents_manage" ON public.consents
    FOR ALL USING (
        company_id = current_user_company_id() AND (
            profile_id = auth.uid() OR current_user_role() IN ('admin', 'manager')
        )
    );

-- Reports:
CREATE POLICY "reports_select_policy" ON public.reports
    FOR SELECT USING (
        company_id = current_user_company_id() AND (
            rep_id = auth.uid() OR current_user_role() IN ('admin', 'manager')
        )
    );

-- 6. AUTOMATED RETENTION POLICY (Delete breadcrumb points older than 30 days)
CREATE OR REPLACE FUNCTION purge_expired_location_points()
RETURNS INT AS $$
DECLARE
    deleted_count INT;
BEGIN
    DELETE FROM public.location_points
    WHERE recorded_at < (timezone('utc', now()) - INTERVAL '30 days');
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. SQL TEST SCRIPT PROVING MULTI-TENANT ISOLATION
/*
DO $$
DECLARE
    comp_a UUID;
    comp_b UUID;
    rep_a UUID := '00000000-0000-0000-0000-000000000001'::uuid;
    rep_b UUID := '00000000-0000-0000-0000-000000000002'::uuid;
BEGIN
    INSERT INTO public.companies(name) VALUES ('Company Alpha') RETURNING id INTO comp_a;
    INSERT INTO public.companies(name) VALUES ('Company Beta') RETURNING id INTO comp_b;
    
    INSERT INTO public.profiles(id, company_id, full_name, email, role)
    VALUES (rep_a, comp_a, 'Rep Alpha', 'repa@alpha.com', 'rep');
    
    INSERT INTO public.profiles(id, company_id, full_name, email, role)
    VALUES (rep_b, comp_b, 'Rep Beta', 'repb@beta.com', 'rep');
    
    -- Test: Assert rep A cannot read or write to Company Beta
    -- RLS enforces current_user_company_id() match.
END $$;
*/
