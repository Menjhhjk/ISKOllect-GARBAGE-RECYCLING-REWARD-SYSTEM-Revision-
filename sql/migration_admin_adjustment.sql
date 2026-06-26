-- ISKOllect — MIGRATION: allow admin bottle-count corrections
-- ============================================================
-- Run this ONCE against your existing iskollect_db database.
-- It widens the points_ledger source check constraint to also
-- accept 'admin_adjustment', which is logged whenever an admin
-- edits a student's bottle count from the Edit Student popup
-- (as opposed to a normal bottle_submission or redemption).
--
-- HOW TO RUN (Windows, from a terminal):
--   "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d iskollect_db -f migration_admin_adjustment.sql
--
-- Safe to re-run: it drops the old constraint first if present.
-- ============================================================

BEGIN;

ALTER TABLE points_ledger
    DROP CONSTRAINT IF EXISTS points_ledger_source_valid;

ALTER TABLE points_ledger
    ADD CONSTRAINT points_ledger_source_valid
        CHECK (source IN ('bottle_submission', 'redemption', 'admin_adjustment'));

COMMIT;

\echo '============================================'
\echo 'Migration complete.'
\echo 'points_ledger now also accepts admin_adjustment as a source.'
\echo '============================================'
