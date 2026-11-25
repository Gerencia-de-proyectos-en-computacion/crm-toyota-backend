-- =============================================================================
-- V3__add_quote_closed_at.sql — Fecha exacta de atención / cierre comercial
-- =============================================================================

ALTER TABLE quotes ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP WITH TIME ZONE;

UPDATE quotes
SET closed_at = updated_at
WHERE status = 'CERRADA' AND closed_at IS NULL;
