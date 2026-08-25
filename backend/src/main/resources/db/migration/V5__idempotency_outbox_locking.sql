CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    response_status INT,
    response_body TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT uq_idempotency_key UNIQUE (tenant_id, idempotency_key)
);

CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    exchange VARCHAR(100) NOT NULL,
    routing_key VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_events_unpublished ON outbox_events (id) WHERE published_at IS NULL;

ALTER TABLE customers ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE products ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
