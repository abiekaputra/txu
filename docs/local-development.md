# Local Development

## Docker Compose path

Use the reference product path from the repository root:

```bash
cp .env.example .env
./scripts/start-local.sh
```

The script preserves an existing local signing key. Database state lives in the
`txu-data` Docker volume.

## Host-native path

Run PostgreSQL on `127.0.0.1:5437` with database and user `txu`, then start the
API with Java 21:

```bash
export DATABASE_URL=jdbc:postgresql://127.0.0.1:5437/txu
export DATABASE_USER=txu
export DATABASE_PASSWORD=txu-local
export TXU_SIGNING_ALLOW_EPHEMERAL=true
export SPRING_PROFILES_ACTIVE=demo
./backend/gradlew -p backend bootRun
```

In a second terminal:

```bash
pnpm install
pnpm --dir web dev
```

Ephemeral keys exist for development and automated validation only. The Compose
path generates persistent local files and is the reference runtime.

## Test layers

- `pnpm --dir web test` validates meaningful component behavior.
- `./backend/gradlew -p backend test` validates canonicalization and signature
  integrity without infrastructure.
- `pnpm test:integration` validates the transaction flow against PostgreSQL.
- `pnpm test:e2e` validates the product through Chromium and writes JPG
  evidence to `artifacts/screenshots/`.

## Troubleshooting

| Symptom                      | Check                                              |
| ---------------------------- | -------------------------------------------------- |
| API does not become healthy  | PostgreSQL health and signing-key file permissions |
| Browser mutation returns 403 | Reload so the session and CSRF token are fresh     |
| Public proof returns invalid | Preserve the key used when the receipt was issued  |
| Port is already occupied     | Free ports 3001, 5437, and 8080                    |
