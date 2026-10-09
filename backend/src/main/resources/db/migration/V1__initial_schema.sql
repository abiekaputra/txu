CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    display_name VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('MEMBER', 'MODERATOR', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE receipts (
    id UUID PRIMARY KEY,
    issuer_id UUID NOT NULL REFERENCES accounts(id),
    recipient_label VARCHAR(80) NOT NULL,
    title VARCHAR(80) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    category VARCHAR(24) NOT NULL CHECK (category IN ('MENTORSHIP', 'TEAMWORK', 'SUPPORT', 'CRAFT', 'OTHER')),
    lifecycle VARCHAR(16) NOT NULL DEFAULT 'DRAFT' CHECK (lifecycle IN ('DRAFT', 'ISSUED', 'REVOKED')),
    moderation_state VARCHAR(16) NOT NULL DEFAULT 'CLEAR' CHECK (moderation_state IN ('CLEAR', 'FLAGGED', 'HIDDEN')),
    public_id VARCHAR(48) UNIQUE,
    schema_version INTEGER,
    key_id VARCHAR(80),
    issued_at TIMESTAMPTZ,
    canonical_payload TEXT,
    payload_sha256 VARCHAR(64),
    signature TEXT,
    acknowledgement_hash VARCHAR(64) UNIQUE,
    acknowledged_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    revocation_reason VARCHAR(240),
    moderation_reason VARCHAR(240),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX receipts_issuer_idx ON receipts(issuer_id, updated_at DESC);
CREATE INDEX receipts_public_idx ON receipts(public_id) WHERE public_id IS NOT NULL;

CREATE TABLE reports (
    id UUID PRIMARY KEY,
    receipt_id UUID NOT NULL REFERENCES receipts(id),
    reason VARCHAR(32) NOT NULL CHECK (reason IN ('HARASSMENT', 'PERSONAL_INFORMATION', 'IMPERSONATION', 'OTHER')),
    details VARCHAR(500),
    fingerprint VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'DISMISSED', 'RESOLVED')),
    decision_reason VARCHAR(240),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    decided_at TIMESTAMPTZ
);

CREATE INDEX reports_queue_idx ON reports(status, created_at);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_id UUID REFERENCES accounts(id),
    actor_label VARCHAR(100) NOT NULL,
    event_type VARCHAR(48) NOT NULL,
    subject_id UUID NOT NULL,
    detail VARCHAR(300),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    correlation_id VARCHAR(80) NOT NULL
);

CREATE INDEX audit_subject_idx ON audit_events(subject_id, occurred_at);

CREATE OR REPLACE FUNCTION prevent_issued_receipt_rewrite() RETURNS trigger AS $$
BEGIN
    IF OLD.lifecycle <> 'DRAFT' AND (
        NEW.recipient_label <> OLD.recipient_label OR
        NEW.title <> OLD.title OR
        NEW.message <> OLD.message OR
        NEW.category <> OLD.category OR
        NEW.canonical_payload <> OLD.canonical_payload OR
        NEW.signature <> OLD.signature
    ) THEN
        RAISE EXCEPTION 'issued receipt content is immutable';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER receipts_immutable_content
BEFORE UPDATE ON receipts
FOR EACH ROW EXECUTE FUNCTION prevent_issued_receipt_rewrite();
