-- Soft-delete em clientes e produtos: excluir passa a marcar deleted_at em
-- vez de apagar a linha de verdade. A aplicação filtra automaticamente
-- (Customer/Product têm @SQLRestriction("deleted_at is null")), então não
-- aparecem mais em nenhuma consulta normal, mas o dado continua no banco.

ALTER TABLE customers ADD COLUMN deleted_at TIMESTAMPTZ;
ALTER TABLE products ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE INDEX idx_customers_deleted_at ON customers (deleted_at);
CREATE INDEX idx_products_deleted_at ON products (deleted_at);
