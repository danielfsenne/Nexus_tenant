#!/usr/bin/env bash
set -euo pipefail

# Restaura um backup gerado pelo backup.sh. SOBRESCREVE o banco de destino.
#
#   ./scripts/restore.sh backups/nexus_20260826_120000.sql.gz
#
# Para restaurar em um banco remoto, exporte DATABASE_URL antes de rodar.

if [ $# -ne 1 ]; then
  echo "Uso: $0 <arquivo-de-backup.sql.gz>"
  exit 1
fi

FILE="$1"
if [ ! -f "$FILE" ]; then
  echo "Arquivo não encontrado: $FILE"
  exit 1
fi

TARGET="Postgres local (container nexus-postgres)"
if [ -n "${DATABASE_URL:-}" ]; then
  TARGET="banco remoto (via DATABASE_URL)"
fi

echo "Isso vai SOBRESCREVER o banco de dados em: $TARGET"
read -r -p "Digite 'sim' para confirmar: " CONFIRM
if [ "$CONFIRM" != "sim" ]; then
  echo "Cancelado."
  exit 1
fi

if [ -n "${DATABASE_URL:-}" ]; then
  gunzip -c "$FILE" | psql "$DATABASE_URL"
else
  gunzip -c "$FILE" | docker exec -i nexus-postgres psql -U nexus -d nexus
fi

echo "Restauração concluída a partir de: $FILE"
