CREATE TABLE invites (
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(150) NOT NULL,
    role         VARCHAR(20) NOT NULL,
    tenant_id    BIGINT NOT NULL REFERENCES tenants(id),
    token        VARCHAR(64) NOT NULL UNIQUE,
    expires_at   TIMESTAMP NOT NULL,
    accepted_at  TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_invites_tenant_id ON invites(tenant_id);
CREATE INDEX idx_invites_token ON invites(token);
