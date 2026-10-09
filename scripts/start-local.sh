#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."
./scripts/generate-signing-keys.sh
docker compose up --build --wait
echo "TXU is ready at http://localhost:3001"

