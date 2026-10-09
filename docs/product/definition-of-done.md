# Definition of Done

TXU is complete only when a reviewer can run the local product, perform its
central journey in a browser, and connect every portfolio claim to code, tests,
or captured evidence.

## Product completeness

- [x] The P0 scope in the product brief is implemented.
- [x] Registration, draft, preview, issue, verify, acknowledge, revoke, report,
      and moderation flows are usable from the web interface.
- [x] No critical journey requires a manual API request or database edit.
- [x] Every acceptance criterion is either proven or explicitly removed from
      scope with a recorded decision.
- [x] Limitations are stated without claiming deployment, legal identity, or
      independent notarization.

## Engineering quality

- [x] Production modules follow single responsibility and domain boundaries.
- [x] Non-Python production files remain at or below 300 lines; test files remain
      at or below 1000 lines.
- [x] TypeScript and supported text files pass Prettier.
- [x] Kotlin passes ktlint and the configured static analysis.
- [x] Database migrations build a clean schema and do not rely on manual steps.
- [x] Error contracts, logs, and metrics are consistent across modules.
- [x] No secret, personal production data, or private key is committed.

## Security and integrity

- [x] Authentication, authorization, CSRF, session, input, and rate-limit checks
      pass their required tests.
- [x] Issued content is immutable through both public and internal application
      interfaces.
- [x] Signature test vectors prove deterministic canonicalization and tamper
      detection.
- [x] Acknowledgement secrets are one-time, hashed at rest, and redacted.
- [x] Audit events exist for issue, acknowledgement, revocation, and moderation.
- [x] The threat model and security documentation match the implementation.

## Verification

- [x] Backend unit and integration suites pass.
- [x] Frontend unit and component suites pass.
- [x] Playwright critical journeys pass against the local service topology.
- [x] Accessibility checks pass on critical pages.
- [x] A fresh checkout installs, builds, migrates, and tests in CI with documented commands.
- [x] The local p95 verification target is measured with a repeatable script.
- [x] CI is green on the default branch.

## Documentation and portfolio evidence

- [x] The repository README explains the story, user value, architecture,
      implemented scope, setup, tests, decisions, and honest status.
- [x] OpenAPI documentation matches the running API.
- [x] Architecture and data-flow diagrams match the final system.
- [x] `DevLab/Porto/TXU` contains screenshots for every important user-facing
      page and state.
- [x] Each screenshot has page, purpose, user action, technical context, and
      result metadata.
- [x] The portfolio narrative is shaped around TXU's strongest idea: a human
      thank-you becoming a verifiable receipt.
- [x] Repository description, topics, social preview, and pinned status are
      reviewed only after the product evidence is ready.

## Release boundary

Public cloud deployment is not part of this definition of done. The required
release is a complete local web product started through Docker Compose with a
documented demo path.
