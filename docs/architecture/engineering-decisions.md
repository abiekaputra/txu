# Engineering Decisions

These records lock the Phase 1 direction. A later change requires a new decision
record describing the evidence and migration impact.

## ADR-001: Build a modular monolith

**Status:** Accepted

**Decision:** Build one Spring Boot backend with explicit identity, receipt,
verification, moderation, audit, and operations modules.

**Reason:** TXU needs transactional consistency more than independent scaling.
One deployable unit keeps the local runtime understandable while module ports
preserve separation of responsibilities.

**Consequence:** Modules share a runtime and PostgreSQL instance. Direct
cross-module table writes remain prohibited.

## ADR-002: Use a separate React web application

**Status:** Accepted

**Decision:** Implement the browser product with React, TypeScript, and Vite.

**Reason:** The interaction includes draft state, previews, link handling,
verification explanations, and moderation workflows. A typed component-based UI
supports those states and demonstrates full-stack boundaries clearly.

**Consequence:** The API contract must be explicit and frontend behavior needs
its own test suite. The local runtime should preserve a same-origin browser
boundary.

## ADR-003: Use server-side browser sessions

**Status:** Accepted

**Decision:** Authenticate members with secure, server-side sessions rather than
browser-stored JWT bearer tokens.

**Reason:** TXU is a browser-first monolith. Sessions simplify revocation and
keep authentication credentials out of JavaScript storage.

**Consequence:** Mutations require CSRF protection and a shared session store in
PostgreSQL or the selected Spring session implementation.

## ADR-004: Sign an immutable receipt snapshot with Ed25519

**Status:** Accepted

**Decision:** On issue, canonicalize a versioned payload and sign it with
Ed25519. Store signature, payload hash, algorithm, and key identifier.

**Reason:** A verifiable receipt is TXU's defining engineering property.
Ed25519 provides compact modern signatures without inventing cryptography.

**Consequence:** Canonical serialization, golden test vectors, key handling, and
historical public-key availability become release requirements.

## ADR-005: Separate proof from operational status

**Status:** Accepted

**Decision:** Keep signed content immutable while storing acknowledgement,
revocation, reporting, and moderation as separate records or state dimensions.

**Reason:** A valid historical signature does not imply that a receipt is still
active or visible. Mixing these concepts would either invalidate signatures or
hide important state.

**Consequence:** Verification responses explain both signature integrity and the
effective product result.

## ADR-006: Use two share links

**Status:** Accepted

**Decision:** Issue an unlisted public verification link and a separate private,
single-use acknowledgement link.

**Reason:** Public verification and recipient authority have different security
needs. One link cannot safely represent both.

**Consequence:** The interface must visually separate the links and warn the
sender that the recipient link grants a one-time capability.

## ADR-007: Deliver copied links instead of email

**Status:** Accepted

**Decision:** The sender copies and delivers links through a channel they choose.

**Reason:** Real email adds provider configuration, delivery behavior, privacy,
and operational work unrelated to TXU's central proof model.

**Consequence:** TXU cannot claim delivery tracking. Email remains optional P1
or P2 work after the complete local product exists.

## ADR-008: Use PostgreSQL and Flyway

**Status:** Accepted

**Decision:** Use PostgreSQL in development, tests, and the reference runtime,
with Flyway for schema history.

**Reason:** Receipt transitions depend on transactions, uniqueness, constraints,
locking, and durable audit data. Using the same database behavior throughout
avoids an unrealistic in-memory substitute.

**Consequence:** Integration tests use Testcontainers and local startup requires
Docker or a compatible PostgreSQL service.

## ADR-009: Keep P0 local-first

**Status:** Accepted

**Decision:** Docker Compose is the release environment; public cloud deployment
is outside the definition of done.

**Reason:** Portfolio value comes from a complete, reproducible user journey and
supported engineering evidence. Hosting would add cost and maintenance without
strengthening the first version's central claim.

**Consequence:** Documentation must state the local-only status clearly, and
screenshots must come from the validated local product.

## ADR-010: Keep the first release web-only

**Status:** Accepted

**Decision:** TXU has no native mobile application in P0.

**Reason:** Its core actions are link-based and responsive web flows cover phone
and desktop use. Native mobile work would duplicate the presentation layer
without adding a distinctive product capability.

**Consequence:** Responsive behavior and mobile viewport browser tests are
mandatory.
