#!/usr/bin/env bash
set -euo pipefail

# Backup do Postgres do Nexus. Por padrão, faz backup do container local do
# docker-compose. Para fazer backup de um banco remoto (ex.: Postgres
# gerenciado no Render), exporte DATABASE_URL antes de rodar:
#
#   DATABASE_URL="postgres://usuario:senha@host:5432/banco" ./scripts/backup.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="$SCRIPT_DIR/../backups"
mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUT_FILE="$BACKUP_DIR/nexus_${TIMESTAMP}.sql.gz"

if [ -n "${DATABASE_URL:-}" ]; then
  echo "Fazendo backup via DATABASE_URL (banco remoto)..."
  pg_dump --clean --if-exists "$DATABASE_URL" | gzip > "$OUT_FILE"
else
  echo "Fazendo backup do Postgres local (container nexus-postgres)..."
  docker exec nexus-postgres pg_dump -U nexus --clean --if-exists nexus | gzip > "$OUT_FILE"
fi

echo "Backup salvo em: $OUT_FILE"
echo "Tamanho: $(du -h "$OUT_FILE" | cut -f1)"
