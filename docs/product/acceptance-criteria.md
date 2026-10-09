# Acceptance Criteria

Every item is intentionally unchecked. It becomes evidence only after the
implementation and automated tests exist.

## AC-01: Account lifecycle

- [ ] A visitor can register with a valid display name, email, and password.
- [ ] Duplicate normalized email addresses are rejected without exposing stored
      account data.
- [ ] Passwords are hashed with the configured adaptive password encoder.
- [ ] Valid credentials create a secure session and invalid credentials return
      a generic error.
- [ ] Logout invalidates the server session.
- [ ] Protected pages and endpoints reject unauthenticated requests.

## AC-02: Draft creation and ownership

- [ ] An authenticated member can create a draft with all required fields.
- [ ] Recipient name is 2–80 characters, title is 5–80 characters, and message
      is 20–1000 characters after normalization.
- [ ] Category accepts only a documented enum value.
- [ ] Validation errors identify the affected field and preserve safe inputs.
- [ ] A member can read, edit, and delete only their own drafts.
- [ ] An issued or revoked receipt cannot be edited or deleted through any API.

## AC-03: Preview and issue

- [ ] Preview shows every field included in the signed payload.
- [ ] Issue requires a separate explicit confirmation action.
- [ ] The server creates the canonical payload, signature, public ID,
      acknowledgement-token hash, and issue audit event atomically.
- [ ] The receipt remains a draft when signing or persistence fails.
- [ ] A successful issue returns the public URL and private recipient URL.
- [ ] The plaintext acknowledgement secret is returned once and never persisted
      or logged.
- [ ] Repeating the issue request cannot create duplicate issued receipts.

## AC-04: Signature integrity

- [ ] Every issued receipt uses Ed25519 and records its schema version and key
      identifier.
- [ ] Canonicalization is deterministic across application restarts.
- [ ] The signature covers receipt public ID, issuer label, recipient label,
      title, message, category, issue timestamp, schema version, and key ID.
- [ ] Changing any signed byte makes verification fail.
- [ ] Mutable acknowledgement, revocation, report, and moderation data is not
      included in the signed payload.
- [ ] The verification service can select the correct configured public key by
      key identifier.

## AC-05: Public verification

- [ ] Anyone with a valid public URL can inspect a visible receipt without
      signing in.
- [ ] The page communicates integrity and effective lifecycle state separately.
- [ ] Valid issued receipts show `VERIFIED`.
- [ ] Valid acknowledged receipts show `VERIFIED` and `ACKNOWLEDGED`.
- [ ] Valid but revoked receipts show `REVOKED` and preserve proof details.
- [ ] Invalid signatures show a prominent `INVALID` warning.
- [ ] Hidden receipts reveal only a neutral moderation notice and no message.
- [ ] Unknown and malformed identifiers do not reveal whether nearby IDs exist.

## AC-06: Acknowledgement

- [ ] A valid private recipient link displays the receipt before confirmation.
- [ ] A recipient can acknowledge an eligible receipt once without an account.
- [ ] Acknowledgement records the timestamp and invalidates the token atomically.
- [ ] Replayed or invalid tokens cannot change state.
- [ ] Revoked or hidden receipts cannot be newly acknowledged.
- [ ] A public verification page never exposes the acknowledgement secret.

## AC-07: Revocation

- [ ] Only the issuer can revoke their issued receipt.
- [ ] Revocation requires a non-empty reason and explicit confirmation.
- [ ] Revocation is terminal and idempotent.
- [ ] Revocation does not delete or mutate the signed payload.
- [ ] Public verification immediately returns the effective revoked result.
- [ ] The action records actor, timestamp, reason, and receipt ID in the audit
      log.

## AC-08: Reporting and moderation

- [ ] A public visitor can submit a valid report from a visible receipt page.
- [ ] Report input is length-limited, normalized, and rate-limited.
- [ ] Repeated equivalent reports from the same coarse fingerprint are
      suppressed for the configured window.
- [ ] Only moderators or administrators can access the report queue.
- [ ] A moderator can dismiss, hide, or restore with a required reason.
- [ ] Hidden content is unavailable to public and recipient views but remains
      available to authorized moderation review.
- [ ] Every moderation decision appends an audit event.

## AC-09: Authorization and security

- [ ] Server-side authorization protects every mutation; hidden UI controls are
      never the only enforcement.
- [ ] Browser mutations require CSRF protection.
- [ ] Session cookies are `HttpOnly`, `SameSite=Lax` or stricter, and `Secure`
      outside the explicit local HTTP profile.
- [ ] Security headers include a restrictive Content Security Policy,
      frame-ancestors protection, MIME sniffing protection, and referrer policy.
- [ ] Logs redact passwords, session identifiers, signing private keys, and
      acknowledgement secrets.
- [ ] Inputs render as text and cannot inject executable markup.
- [ ] Authentication, reporting, and recipient-token endpoints are rate-limited.

## AC-10: Reliability and observability

- [ ] Readiness fails when the database or active signing key is unavailable.
- [ ] Liveness does not depend on external services that P0 does not use.
- [ ] Logs are structured and include a request correlation ID.
- [ ] Metrics expose request latency, error count, receipt issue outcomes, and
      verification outcomes without user content.
- [ ] Errors use a stable machine-readable envelope and a safe human message.
- [ ] Database migrations are repeatable from an empty PostgreSQL instance.

## AC-11: Web experience

- [ ] Critical flows work at mobile, tablet, and desktop viewport widths.
- [ ] Keyboard navigation and visible focus work on every interactive control.
- [ ] Status is communicated by text and icon as well as color.
- [ ] Loading, empty, validation, success, forbidden, conflict, and unavailable
      states are intentionally designed.
- [ ] Copy-link actions provide centered, readable feedback within their status
      component rather than misaligned text.
- [ ] Destructive and irreversible actions use clear confirmation language.

## AC-12: Automated evidence

- [ ] Unit tests cover canonicalization, signature verification, validation, and
      domain transitions.
- [ ] PostgreSQL integration tests cover repositories, transactions, and access
      control.
- [ ] API contract tests cover success and error envelopes.
- [ ] Frontend tests cover meaningful behavior and accessibility states.
- [ ] Playwright covers register, issue, verify, acknowledge, revoke, report,
      hide, and restore journeys.
- [ ] CI runs formatting, linting, tests, build, secret scan, and dependency
      checks.
