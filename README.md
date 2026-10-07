Fieldly: Product and Technical Documentation
Version: 1.0 (Planning and Build Blueprint)
 Date: October 2026
 Project Lead: Mohammad Mustakim, Team Alpha
 Status: Concept complete, ready for prototype build

1. Overview
Fieldly is a mobile-first field sales coordination app. It lets a sales manager see where their team is, check that visits really happened, and get daily reports without making phone calls or reading hand-written summaries.
An easy way to picture it: when you order food on Pathao or Foodpanda, you do not call the rider to ask where they are. You open the app and see the rider moving on a map, and you get a message when the food arrives. AlphaLink gives sales managers that same calm feeling, and adds proof that each visit happened.
One-line promise: A manager should rarely have to call the team just to know what the team is doing.
Platforms and order of release:
Android app (Google Play), first
iPhone app (App Store), second
Web app for desktop computers, third (mainly for managers)

2. The Problem
Field sales teams in pharma, FMCG distribution, insurance and microfinance work outside the office all day. The moment a rep leaves, the manager loses sight of them. This causes four problems:
Managers do not know if visits are really happening.
End of day reports are written by hand and cannot be checked.
Two reps may cover the same area while another area is ignored.
Reps waste time writing reports and calling the manager.
Current tools do not fit. WhatsApp has no location proof and no structured records. Google Maps is for navigation, not team management. Paper and phone calls depend on trust. Enterprise field software works well but is priced for big foreign companies, not for most businesses in Bangladesh.
(Add sources for your market numbers, such as rep counts in pharma and microfinance, before the final presentation. Unsourced numbers are easy to attack.)

3. Goals and Non-Goals
Product goals
Give managers a live, trusted view of the field team.
Make false visit reporting hard to do and easy to spot.
Remove manual report writing for reps.
Work well on cheap Android phones and weak internet.
Be easy for non-technical teams to start using within one hour.
Be fully usable in Bangla and English.
Build goals
Build the MVP with free tools only (except store fees).
Keep the backend as the long-term asset, so screens can change without starting over.
Keep code organized so it can be updated and extended safely.
Non-goals for version 1 (said clearly so the project stays small)
No voice or video calling.
No order taking, payments, or expense claims yet.
No Bluetooth or mesh offline network.
No AI analytics or route optimization yet.
Success measures for the pilot
A pilot team of 10 to 20 reps uses it for at least 4 weeks.
At least 90% of planned visits are checked in through the app.
Manager phone calls "asking where are you" drop noticeably (ask the manager to estimate before and after).
Battery use under about 8 to 10% for a full work day.
Check-in confirms in under 2 seconds on a normal connection.

4. Users
Role
Who they are
What they need
Rep
Field worker, often on a cheap Android phone
Simple app, clear daily list, one-tap check-in, no report writing, low battery use
Manager
Team lead, uses phone and often a computer
Live map, visit proof, dashboard, daily report
Admin
Company owner or office head
Create team, add reps and clients, set rules, manage billing later

Target industries: pharmaceutical sales, FMCG distribution, insurance field agents, microfinance field officers. Later: construction supervision, logistics, NGO field teams.

5. Features
5.1 Version 1 (MVP, Android)
1. Login and roles. Admin invites managers and reps by email. Each person sees only what their role allows.
2. Start Day and End Day. Tracking runs only between these two taps. This is the privacy promise made real.
3. Live Team Map. Reps appear as labeled markers. Update rate is adaptive: about every 15 to 30 seconds when moving, much less when standing still. Tapping a marker shows name, status, last update, and today's progress.
4. Visit Plan. The manager assigns today's list of clients to each rep. Without a plan, "visits remaining" has no meaning.
5. Client list. Manager or rep adds a client by dropping a pin on the map, or by tapping "Save my current location" while standing at the client's place. No paid address search is needed.
6. GPS Verified Check-In. Rep taps Check-In at a client. The server checks distance and GPS accuracy (see rules in 7.1). The visit is saved with time, location, and an optional photo.
7. Live Performance Dashboard. For each rep: visits done, visits left, time spent per client, and flags.
8. Bangla and English. Full interface in both languages, switchable at any time.
5.2 Version 1.1
Automatic End of Day Report (PDF plus email and in-app), sent at a time the company sets.
Push notifications (for example "plan updated", or "rep has been still for 45 minutes").
Weak internet mode: check-ins are saved on the phone and sent later with the original time.
Photo proof with timestamp for low-accuracy check-ins.
Suspicious check-in flags shown to the manager.
iPhone release.
5.3 Version 2 and later
Desktop web dashboard (or improved separate dashboard).
Order taking, expense and mileage claims.
Route replay (watch a rep's day like a video).
Excel export, company branding (white label).
Territory drawing on the map and coverage heatmaps.

6. User Flows
Rep, daily flow
Open app, tap Start Day.
See today's visit list.
Travel to a client. App tracks quietly in the background.
At the client, tap Check-In. App checks location, shows a green success screen. Add photo or note if wanted.
Repeat for each client.
Tap End Day. See a short summary. The report is created automatically.
Manager, daily flow
Create or adjust today's visit plans (often the night before).
During the day, open the live map and dashboard when needed.
Receive alerts only when something looks wrong.
Evening: receive the verified report.
Admin, setup flow
Create the company.
Invite managers and reps.
Add clients.
Set rules: working hours, check-in distance, report time.

7. Key Business Rules
7.1 Check-In rule
Plain version: the rep must be close enough to the client, and the GPS reading must be trustworthy enough.
Default allowed distance: 50 meters (company can change it, for example between 30 and 150 meters).
The phone also reports GPS accuracy (how sure it is). The server uses distance minus accuracy, so an honest rep in a crowded street is not unfairly rejected.
If accuracy is very poor, the app does not just reject. It asks for a timestamped photo, and the visit is saved with a "needs review" flag.
The check is done on the server, not only on the phone, because anything on the phone can be tampered with.
7.2 Tracking rule
Tracking runs only after Start Day and stops after End Day (or at the company's set end time).
The rep can see exactly what is collected, on a clear privacy screen.
Detailed location trails are kept for a limited time (default 14 to 30 days on the free plan, up to 90 days later). After that they are shrunk into one small summary per day.
7.3 Save-a-point rule
The phone saves a new location point only if the rep moved at least about 30 meters, or a set time passed. This cuts battery, data size and cost.

8. Screens
Rep app: Login, Home with Start Day, Today's Visit List, Client Detail with Check-In, Check-In Success, My Day Summary, Settings and Privacy.
Manager (phone and web): Live Map with rep list sheet, Team Dashboard, Rep Detail, Visit Plan Builder, Clients List and Add Client, Reports, Settings.
Admin: Team Management, Rules and Settings.

9. UI and UX Guidelines
Feel: clean, calm, premium. Lots of white space, rounded cards, one main color, no clutter. Think Uber and Pathao for the map experience, and Notion or Linear for dashboard cleanliness.
Design system (give this to the AI first and make it follow it everywhere):
Base: Material 3 in Flutter, customized.
Colors: Main deep navy #1E2A5A, accent #4F6BFF, success #12B76A, warning #F79009, error #F04438, background #F6F7FB. Full dark mode.
Fonts: Inter or Plus Jakarta Sans (English), Hind Siliguri or Noto Sans Bengali (Bangla). Test Bangla early.
Shapes: large rounded corners, soft shadows, consistent spacing.
Patterns to use
Map as full-screen hero, with a draggable bottom sheet for the rep list.
Custom round markers with initials or photo and a colored status ring.
Bento style dashboard tiles on the manager side.
Big one-thumb Check-In button at the bottom on the rep side.
Skeleton loading (gray placeholder shapes) instead of spinners.
Quick animations of 200 to 300 ms, soft vibration on successful check-in, friendly empty states.
Keep animations light so cheap phones stay smooth.
Quality rule: design screen by screen, never "the whole app in one prompt." Test on a real cheap phone.

10. Technical Architecture
10.1 Technology stack
Layer
Choice
Why
Mobile app
Flutter (Dart)
Real native speed, one codebase for Android, iPhone and web, strong AI support
Backend and database
Supabase (PostgreSQL)
Login, database, storage and live updates in one place, strong security rules
Map math
PostGIS (inside the database)
Free, accurate distance checks
Server logic
Supabase Edge Functions (TypeScript)
Check-in validation, report generation
Map display
flutter_map or MapLibre
Free, styleable, works on mobile and web
Map tiles
OpenStreetMap data, Protomaps file hosted on Cloudflare
No per-view fee, full control of style
Push notifications
Firebase Cloud Messaging
Free, standard
Web hosting
Cloudflare Pages or Firebase Hosting
Free tier
Email reports
Resend or Brevo
Free monthly allowance
Code storage
GitHub
Version history, safe rollbacks
iPhone builds
Cloud builder (for example Codemagic)
No Mac needed

Why not a plain website for the rep app? Websites cannot track location reliably in the background, so the core feature would fail. A real mobile app is required.
10.2 How data moves
Think of the rep's phone as a delivery person carrying small parcels. It collects location points and sends them in small batches about once a minute, not one by one. Only the newest position is pushed live to the manager's map. The full trail is saved for history. Check-ins are sent immediately and confirmed by the server.
Rep phone (Flutter)
   | batches of points, check-ins (HTTPS)
   v
Supabase: Auth + Database (Postgres + PostGIS) + Storage + Realtime
   |                         |
   | live latest position    | scheduled jobs: end of day report, cleanup
   v                         v
Manager phone / Web app     Email + PDF + Push notification

10.3 Main database tables
Table
Holds
companies
company name, settings (distance rule, work hours, report time)
profiles
user, role (admin, manager, rep), company, language
clients
name, type, saved location, assigned team
visit_plans / plan_items
which rep visits which client on which day, order
day_sessions
Start Day and End Day times per rep
visits
check-in time, location, GPS accuracy, distance, photo link, status, flags
location_points
detailed trail (short retention)
live_positions
only the latest position per rep (fast map reads)
flags
suspicious events and reasons
reports
generated daily reports and files
consents
who agreed to tracking and when
audit_logs
who changed what

Every table carries a company_id so one company can never see another company's data.

11. Security and Privacy
Security works in layers, like a bank: door, guard, camera, vault.
Data separation (most important). Turn on Row Level Security for every table. The database itself refuses wrong requests, even if the app has a bug. Rep sees only own data, manager only own team, nobody sees other companies.
Login. Invite-only accounts, strong passwords, optional two step login for managers.
Encrypted traffic and storage. All connections use HTTPS. Stored data is encrypted by the cloud provider (do not claim to build AES-256 yourself, say it is provided by the platform).
Secrets. Secret keys live only on the server, never inside the app code.
Private photos. Photos go in private storage and open only through short-life links.
Fake GPS defense. Detect mock location apps, rooted or modified phones, and fake app copies (using Google Play Integrity), and flag impossible movement (for example 20 km in 10 seconds). Suspicious events are flagged for the manager, not silently hidden. Be honest in the pitch: AlphaLink makes cheating hard and visible, it does not claim cheating is impossible.
Abuse limits. Rate limits so nobody can flood the server.
Backups and logs. Daily backups (on a paid plan) and an audit log of changes.
Privacy by design. Work-hours-only tracking, a clear "what we collect" screen, written consent at sign up, automatic deletion of old trails, and a published privacy policy page (also required by Google Play).
Human review. Before real customers use it, have a technical person spend a few hours checking the security setup. AI-written code can hide holes.

12. Performance and Scalability
Targets (give these to the AI as goals):
App opens in under 3 seconds on a cheap Android phone.
Check-in confirms in under 2 seconds.
Live map delay under 5 seconds.
Battery use under about 8 to 10% over a full work day.
Scale plan. One rep creates roughly 1,900 points a day at a 15 second rate. That is small for 20 reps (about 38,000 rows) but large for 1,000 reps (about 2 million rows a day). So: move-at-least-30m rule, short retention for detailed trails, daily summaries for old data, indexes on location and date, and a separate live_positions table so the map never scans the big trail table.
Hardest technical risk: background tracking on cheap Android phones. Brands like Xiaomi, Oppo, Realme and Samsung stop background apps aggressively. Use an Android foreground service (with a visible notification), guide the rep to disable battery optimization for the app, and test on those brands early.

13. Cost Plan (Free as Much as Possible)
Item
Cost
Google Play developer account
one-time fee (about $25)
Apple developer account
yearly fee (about $99), delay until ready for iPhone
Supabase, Firebase messaging, Cloudflare, GitHub, email sending
free plans for MVP and pilot
Domain name
optional, free subdomain at first

Free plan rules to remember (check current limits before relying on them): Supabase free projects can pause when unused and have a database size limit (around 500 MB), so keep trail retention short in the pilot and move to a paid plan when the first customer pays. Do not use Google Maps, since it needs billing. Do not use free address search for clients, use pin dropping instead.

14. Release Plan and Store Requirements
Google Play. New personal developer accounts have needed a closed test with about 12 testers for about 14 days before production access. Check today's rule and start collecting testers early. Because the app uses background location, prepare: an in-app message explaining why before the permission request, a public privacy policy page, and a Play Console explanation (often with a short demo video).
Apple App Store. Stricter on background location and review. Release only after the Android version is stable. Expect a few review rounds.
Desktop web app. Start with the Flutter web build from the same code. If the dashboard feels heavy, build a separate web dashboard (for example with Next.js) on the same Supabase backend. This is cheap to do because the backend and database hold the real value.

15. Build Plan (Vibe Coding for a Non-Programmer)
Working rules
One feature per prompt. Never "build the whole app."
Save to GitHub after every working step.
Paste this document (or a short summary) at the start of each AI session so the AI keeps the rules.
Test every feature on a real phone, outside, while walking.
After each feature, ask the AI to review for security, especially Row Level Security and exposed keys.
When something breaks, give the AI the exact error message.
Check what Google AI Studio can export today. If it works better for web apps than Flutter phone apps, use it for planning and code pieces, then run the project in an editor like Antigravity or Cursor.
Build order
Project setup, design system file (colors, fonts, spacing).
Supabase setup, tables, Row Level Security.
Login and roles, invite flow.
Rep home, Start Day and End Day.
Background location tracking and batch upload.
Live map for managers.
Clients (drop pin, save current location).
Visit plans.
Check-In with server checks.
Dashboard.
Bangla and English.
Polish, test on cheap phones, security review.
Reports, notifications, weak internet mode (v1.1).
Closed testing and Play Store release.

16. Testing Plan
Walk tests: carry two or three phones, walk real routes, compare tracked paths.
Device tests: at least one cheap phone each from Xiaomi, Samsung, Realme or Oppo.
Weak internet tests: airplane mode during check-in, then reconnect.
Cheating tests: try a fake GPS app yourself and confirm it is flagged.
Permission tests: two companies, confirm they can never see each other's data.
Battery tests: full 8 hour day with tracking on.
Pilot: 10 to 20 real reps for 4 weeks, collect feedback weekly.

17. Business Model
Freemium: free for small teams (for example up to 3 reps), paid for larger teams.
Pricing: per rep per month, shown in taka, easy to understand and grows with the customer.
Payments: bKash, Nagad and bank transfer, not only cards.
Extra income later: white label licensing, custom integrations, onboarding and training.
Real competition: not enterprise software, but "WhatsApp plus a notebook plus trust." Sales message: set up in an hour, in Bangla, affordable, with proof for managers.

18. Risks
Risk
Level
Plan
Background tracking killed on cheap phones
High
Foreground service, battery guide, early testing on many brands
Play Store approval delays (testers, background location review)
High
Start tester group early, prepare privacy policy and demo video
Reps dislike being tracked
Medium
Work-hours-only tracking, transparency screen, sell the "no more reports" benefit
AI-written code has security holes
High
Row Level Security always on, security review after each feature, expert review before launch
Free plan limits reached
Medium
Short trail retention, monitor usage, upgrade when revenue starts
Fake GPS cheating
Medium
Multi-layer detection, photo proof, honest claims
Scope creep
Medium
Keep to the MVP list, move new ideas to Version 2


19. Roadmap
Phase
Content
Phase 1 (MVP)
Android app: login, Start/End Day, tracking, live map, clients, visit plans, check-in, dashboard, Bangla/English
Phase 2
Reports, push notifications, weak internet mode, photo proof, suspicious flags, iPhone release
Phase 3
Desktop web dashboard
Phase 4
Orders, expenses, route replay, Excel export, white label


20. Glossary (Simple)
Background tracking: the app keeps tracking location while the screen is off.
Row Level Security: database rules that block users from seeing data that is not theirs.
PostGIS: a database add-on for map math, like distance between points.
Map tiles: small picture squares that make up a map.
Foreground service: an Android feature that keeps an app running with a visible notification.
Flutter: Google's tool for building Android, iPhone and web apps from one code base.
Supabase: a ready-made backend with login, database, storage and live updates.
MVP: the smallest version that is useful and can be tested with real users.
Vibe coding: building software by describing what you want to an AI and guiding it step by step.

21. Updated Q&A for Presentation
Why not WhatsApp or Google Maps? They give no structured records and no location proof. Fieldly checks presence on the server and builds reports automatically.
Can GPS be faked? Any GPS can, so we do not claim otherwise. We detect fake location apps and modified phones, flag impossible movement, and ask for photo proof when accuracy is poor.
Is it expensive to run? The MVP runs on free plans, except the store fees. Costs start when real customers and larger data arrive, and revenue comes at the same time.
What about weak internet? Check-ins are saved on the phone and sent later with the original time.
What about privacy? Tracking only runs between Start Day and End Day, reps can see what is collected, and old trails are deleted automatically.
Have you built it? Not yet. This project covers research, design, technical plan and business plan. The next step is a working prototype of the first four build steps.
Why Flutter and Supabase? One code base for phone and web, a trusted database, and built-in security rules.

Fieldly, Team Alpha, October 2026
When you are ready, I can turn section 15 into the exact copy-paste prompts for the AI, one per step, starting with the design system file.

