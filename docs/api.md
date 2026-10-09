# API Surface

The running API publishes OpenAPI at `/v3/api-docs` and Swagger UI at `/docs`.

| Method | Path                                      | Access             | Purpose                   |
| ------ | ----------------------------------------- | ------------------ | ------------------------- |
| GET    | `/api/session`                            | Public             | Session and CSRF state    |
| POST   | `/api/auth/register`                      | Public + CSRF      | Create a member account   |
| POST   | `/api/auth/login`                         | Public + CSRF      | Start a server session    |
| POST   | `/api/auth/logout`                        | Session + CSRF     | Invalidate the session    |
| GET    | `/api/receipts`                           | Member             | List owned receipts       |
| POST   | `/api/receipts`                           | Member + CSRF      | Create a draft            |
| PATCH  | `/api/receipts/{id}`                      | Owner + CSRF       | Edit an owned draft       |
| DELETE | `/api/receipts/{id}`                      | Owner + CSRF       | Delete an owned draft     |
| POST   | `/api/receipts/{id}/issue`                | Owner + CSRF       | Sign and issue a draft    |
| POST   | `/api/receipts/{id}/revoke`               | Owner + CSRF       | Revoke an issued receipt  |
| GET    | `/api/public/receipts/{publicId}`         | Public             | Verify signed content     |
| POST   | `/api/public/receipts/{publicId}/reports` | Public + CSRF      | Report visible content    |
| GET    | `/api/acknowledgements/{token}`           | Private capability | Preview recipient action  |
| POST   | `/api/acknowledgements/{token}`           | Capability + CSRF  | Acknowledge once          |
| GET    | `/api/moderation/reports`                 | Moderator          | Review report queue       |
| POST   | `/api/moderation/reports/{id}/decision`   | Moderator + CSRF   | Dismiss, hide, or restore |
| GET    | `/api/audit`                              | Moderator          | Read recent audit events  |

Errors use a stable JSON envelope with a code, safe message, and optional field
errors. Correlation IDs are returned and included in server context.
