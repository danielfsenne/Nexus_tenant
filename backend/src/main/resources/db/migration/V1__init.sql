CREATE TABLE tenants (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    plan        VARCHAR(20) NOT NULL DEFAULT 'FREE',
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    tenant_id   BIGINT NOT NULL REFERENCES tenants(id),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email_tenant UNIQUE (email, tenant_id)
);

CREATE TABLE customers (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(150),
    tenant_id   BIGINT NOT NULL REFERENCES tenants(id),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    price       NUMERIC(12,2) NOT NULL,
    tenant_id   BIGINT NOT NULL REFERENCES tenants(id),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  BIGINT NOT NULL REFERENCES customers(id),
    total        NUMERIC(12,2) NOT NULL,
    tenant_id    BIGINT NOT NULL REFERENCES tenants(id),
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_customers_tenant_id ON customers(tenant_id);
CREATE INDEX idx_products_tenant_id ON products(tenant_id);
CREATE INDEX idx_orders_tenant_id ON orders(tenant_id);
