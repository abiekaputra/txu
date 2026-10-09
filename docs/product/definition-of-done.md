# Definition of Done

TXU is complete only when a reviewer can run the local product, perform its
central journey in a browser, and connect every portfolio claim to code, tests,
or captured evidence.

## Product completeness

- [ ] The P0 scope in the product brief is implemented.
- [ ] Registration, draft, preview, issue, verify, acknowledge, revoke, report,
      and moderation flows are usable from the web interface.
- [ ] No critical journey requires a manual API request or database edit.
- [ ] Every acceptance criterion is either proven or explicitly removed from
      scope with a recorded decision.
- [ ] Limitations are stated without claiming deployment, legal identity, or
      independent notarization.

## Engineering quality

- [ ] Production modules follow single responsibility and domain boundaries.
- [ ] Non-Python production files remain at or below 300 lines; test files remain
      at or below 1000 lines.
- [ ] TypeScript and supported text files pass Prettier.
- [ ] Kotlin passes ktlint and the configured static analysis.
- [ ] Database migrations build a clean schema and do not rely on manual steps.
- [ ] Error contracts, logs, and metrics are consistent across modules.
- [ ] No secret, personal production data, or private key is committed.

## Security and integrity

- [ ] Authentication, authorization, CSRF, session, input, and rate-limit checks
      pass their required tests.
- [ ] Issued content is immutable through both public and internal application
      interfaces.
- [ ] Signature test vectors prove deterministic canonicalization and tamper
      detection.
- [ ] Acknowledgement secrets are one-time, hashed at rest, and redacted.
- [ ] Audit events exist for issue, acknowledgement, revocation, and moderation.
- [ ] The threat model and security documentation match the implementation.

## Verification

- [ ] Backend unit and integration suites pass.
- [ ] Frontend unit and component suites pass.
- [ ] Playwright critical journeys pass against the Compose runtime.
- [ ] Accessibility checks pass on critical pages.
- [ ] A fresh-clone setup rehearsal completes with documented commands.
- [ ] The local p95 verification target is measured with a repeatable script.
- [ ] CI is green on the default branch.

## Documentation and portfolio evidence

- [ ] The repository README explains the story, user value, architecture,
      implemented scope, setup, tests, decisions, and honest status.
- [ ] OpenAPI documentation matches the running API.
- [ ] Architecture and data-flow diagrams match the final system.
- [ ] `DevLab/Porto/TXU` contains screenshots for every important user-facing
      page and state.
- [ ] Each screenshot has page, purpose, user action, technical context, and
      result metadata.
- [ ] The portfolio narrative is shaped around TXU's strongest idea: a human
      thank-you becoming a verifiable receipt.
- [ ] Repository description, topics, social preview, and pinned status are
      reviewed only after the product evidence is ready.

## Release boundary

Public cloud deployment is not part of this definition of done. The required
release is a complete local web product started through Docker Compose with a
documented demo path.
