-- Row-Level Security como segunda camada de isolamento multi-tenant, além do
-- filtro por tenant_id já feito em todo repositório/serviço da aplicação.
--
-- Escopo: só as tabelas que são SEMPRE acessadas com um tenant já conhecido
-- (autenticado via JWT). Ficam de fora "users" (login busca por e-mail sem
-- tenant conhecido ainda), "invites"/tokens de senha/e-mail/refresh (busca
-- por token opaco, pré-autenticação), "idempotency_keys"/"outbox_events"
-- (infra interna, nunca exposta em endpoint de leitura) e "tenants" (registro
-- cria uma linha nova sem tenant conhecido ainda).

-- DO em vez de CREATE ROLE direto: Postgres não tem "CREATE ROLE IF NOT
-- EXISTS", e isso deixa seguro rodar mesmo se a role já tiver sido criada
-- manualmente antes (ex.: contorno documentado no DEPLOY.md pra bancos
-- gerenciados onde a role de migration não tem privilégio de CREATE ROLE).
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'nexus_app') THEN
        CREATE ROLE nexus_app LOGIN PASSWORD '${appDbPassword}' NOSUPERUSER NOBYPASSRLS;
    END IF;
END $$;

GRANT USAGE ON SCHEMA public TO nexus_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO nexus_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO nexus_app;

ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON customers
    -- NULLIF: quando a sessão nunca setou app.tenant_id (ou foi resetada),
    -- current_setting com missing_ok=true retorna string vazia, não NULL —
    -- um cast direto pra bigint quebraria com "invalid input syntax". Isso
    -- transforma '' em NULL antes do cast, e tenant_id = NULL nunca casa
    -- (fail-closed), que é o comportamento desejado quando não há tenant.
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint);

ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE products FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON products
    -- NULLIF: quando a sessão nunca setou app.tenant_id (ou foi resetada),
    -- current_setting com missing_ok=true retorna string vazia, não NULL —
    -- um cast direto pra bigint quebraria com "invalid input syntax". Isso
    -- transforma '' em NULL antes do cast, e tenant_id = NULL nunca casa
    -- (fail-closed), que é o comportamento desejado quando não há tenant.
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint);

ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON orders
    -- NULLIF: quando a sessão nunca setou app.tenant_id (ou foi resetada),
    -- current_setting com missing_ok=true retorna string vazia, não NULL —
    -- um cast direto pra bigint quebraria com "invalid input syntax". Isso
    -- transforma '' em NULL antes do cast, e tenant_id = NULL nunca casa
    -- (fail-closed), que é o comportamento desejado quando não há tenant.
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint);

ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON audit_logs
    -- NULLIF: quando a sessão nunca setou app.tenant_id (ou foi resetada),
    -- current_setting com missing_ok=true retorna string vazia, não NULL —
    -- um cast direto pra bigint quebraria com "invalid input syntax". Isso
    -- transforma '' em NULL antes do cast, e tenant_id = NULL nunca casa
    -- (fail-closed), que é o comportamento desejado quando não há tenant.
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint);
