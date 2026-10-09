# Acceptance Criteria

Checked items are backed by the implementation, automated tests, browser evidence, or the documented local release boundary.

## AC-01: Account lifecycle

- [x] A visitor can register with a valid display name, email, and password.
- [x] Duplicate normalized email addresses are rejected without exposing stored
      account data.
- [x] Passwords are hashed with the configured adaptive password encoder.
- [x] Valid credentials create a secure session and invalid credentials return
      a generic error.
- [x] Logout invalidates the server session.
- [x] Protected pages and endpoints reject unauthenticated requests.

## AC-02: Draft creation and ownership

- [x] An authenticated member can create a draft with all required fields.
- [x] Recipient name is 2–80 characters, title is 5–80 characters, and message
      is 20–1000 characters after normalization.
- [x] Category accepts only a documented enum value.
- [x] Validation errors identify the affected field and preserve safe inputs.
- [x] A member can read, edit, and delete only their own drafts.
- [x] An issued or revoked receipt cannot be edited or deleted through any API.

## AC-03: Preview and issue

- [x] Preview shows every field included in the signed payload.
- [x] Issue requires a separate explicit confirmation action.
- [x] The server creates the canonical payload, signature, public ID,
      acknowledgement-token hash, and issue audit event atomically.
- [x] The receipt remains a draft when signing or persistence fails.
- [x] A successful issue returns the public URL and private recipient URL.
- [x] The plaintext acknowledgement secret is returned once and never persisted
      or logged.
- [x] Repeating the issue request cannot create duplicate issued receipts.

## AC-04: Signature integrity

- [x] Every issued receipt uses Ed25519 and records its schema version and key
      identifier.
- [x] Canonicalization is deterministic across application restarts.
- [x] The signature covers receipt public ID, issuer label, recipient label,
      title, message, category, issue timestamp, schema version, and key ID.
- [x] Changing any signed byte makes verification fail.
- [x] Mutable acknowledgement, revocation, report, and moderation data is not
      included in the signed payload.
- [x] The verification service can select the correct configured public key by
      key identifier.

## AC-05: Public verification

- [x] Anyone with a valid public URL can inspect a visible receipt without
      signing in.
- [x] The page communicates integrity and effective lifecycle state separately.
- [x] Valid issued receipts show `VERIFIED`.
- [x] Valid acknowledged receipts show `VERIFIED` and `ACKNOWLEDGED`.
- [x] Valid but revoked receipts show `REVOKED` and preserve proof details.
- [x] Invalid signatures show a prominent `INVALID` warning.
- [x] Hidden receipts reveal only a neutral moderation notice and no message.
- [x] Unknown and malformed identifiers do not reveal whether nearby IDs exist.

## AC-06: Acknowledgement

- [x] A valid private recipient link displays the receipt before confirmation.
- [x] A recipient can acknowledge an eligible receipt once without an account.
- [x] Acknowledgement records the timestamp and invalidates the token atomically.
- [x] Replayed or invalid tokens cannot change state.
- [x] Revoked or hidden receipts cannot be newly acknowledged.
- [x] A public verification page never exposes the acknowledgement secret.

## AC-07: Revocation

- [x] Only the issuer can revoke their issued receipt.
- [x] Revocation requires a non-empty reason and explicit confirmation.
- [x] Revocation is terminal and idempotent.
- [x] Revocation does not delete or mutate the signed payload.
- [x] Public verification immediately returns the effective revoked result.
- [x] The action records actor, timestamp, reason, and receipt ID in the audit
      log.

## AC-08: Reporting and moderation

- [x] A public visitor can submit a valid report from a visible receipt page.
- [x] Report input is length-limited, normalized, and rate-limited.
- [x] Repeated equivalent reports from the same coarse fingerprint are
      suppressed for the configured window.
- [x] Only moderators or administrators can access the report queue.
- [x] A moderator can dismiss, hide, or restore with a required reason.
- [x] Hidden content is unavailable to public and recipient views but remains
      available to authorized moderation review.
- [x] Every moderation decision appends an audit event.

## AC-09: Authorization and security

- [x] Server-side authorization protects every mutation; hidden UI controls are
      never the only enforcement.
- [x] Browser mutations require CSRF protection.
- [x] Session cookies are `HttpOnly`, `SameSite=Lax` or stricter, and `Secure`
      outside the explicit local HTTP profile.
- [x] Security headers include a restrictive Content Security Policy,
      frame-ancestors protection, MIME sniffing protection, and referrer policy.
- [x] Logs redact passwords, session identifiers, signing private keys, and
      acknowledgement secrets.
- [x] Inputs render as text and cannot inject executable markup.
- [x] Authentication, reporting, and recipient-token endpoints are rate-limited.

## AC-10: Reliability and observability

- [x] Readiness fails when the database or active signing key is unavailable.
- [x] Liveness does not depend on external services that P0 does not use.
- [x] Logs are structured and include a request correlation ID.
- [x] Metrics expose request latency, error count, receipt issue outcomes, and
      verification outcomes without user content.
- [x] Errors use a stable machine-readable envelope and a safe human message.
- [x] Database migrations are repeatable from an empty PostgreSQL instance.

## AC-11: Web experience

- [x] Critical flows work at mobile, tablet, and desktop viewport widths.
- [x] Keyboard navigation and visible focus work on every interactive control.
- [x] Status is communicated by text and icon as well as color.
- [x] Loading, empty, validation, success, forbidden, conflict, and unavailable
      states are intentionally designed.
- [x] Copy-link actions provide centered, readable feedback within their status
      component rather than misaligned text.
- [x] Destructive and irreversible actions use clear confirmation language.

## AC-12: Automated evidence

- [x] Unit tests cover canonicalization, signature verification, validation, and
      domain transitions.
- [x] PostgreSQL integration tests cover repositories, transactions, and access
      control.
- [x] API contract tests cover success and error envelopes.
- [x] Frontend tests cover meaningful behavior and accessibility states.
- [x] Playwright covers register, issue, verify, acknowledge, revoke, report,
      hide, and restore journeys.
- [x] CI runs formatting, linting, tests, build, secret scan, and dependency
      checks.
