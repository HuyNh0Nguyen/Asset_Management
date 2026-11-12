#!/usr/bin/env bash
# Xuất hợp đồng OpenAPI từ backend đang chạy ra contracts/openapi.json
set -euo pipefail

# Thư mục gốc repo = cha của scripts/
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_DOCS_URL="${API_DOCS_URL:-http://localhost:8080/api/v3/api-docs}"
OUT_FILE="$ROOT_DIR/contracts/openapi.json"

curl -fsS "$API_DOCS_URL" -o "$OUT_FILE"
echo "Exported OpenAPI contract to $OUT_FILE"