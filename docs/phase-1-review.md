# Phase 1 Review

## Outcome

Phase 1 is complete. TXU now has a bounded product definition and an architecture
that can be implemented without reopening foundational product questions.

## Deliverable review

| Required result                    | Evidence                                              | Status   |
| ---------------------------------- | ----------------------------------------------------- | -------- |
| Problem and value proposition      | `product/product-brief.md`                            | Complete |
| Personas and user needs            | `product/product-brief.md`, `product/user-stories.md` | Complete |
| P0, P1, P2, and non-goals          | `product/product-brief.md`                            | Complete |
| End-to-end user flows              | `product/user-flows.md`                               | Complete |
| Testable acceptance criteria       | `product/acceptance-criteria.md`                      | Complete |
| Product definition of done         | `product/definition-of-done.md`                       | Complete |
| Architecture and module boundaries | `architecture/system-architecture.md`                 | Complete |
| Domain states and invariants       | `architecture/domain-model.md`                        | Complete |
| Threats and controls               | `architecture/security-model.md`                      | Complete |
| Key technical decisions            | `architecture/engineering-decisions.md`               | Complete |
| Page and state wireframes          | `design/wireframes.md`                                | Complete |

## Decisions locked for Phase 2

- TXU is a responsive web product with no native mobile application.
- The backend is a Kotlin and Spring Boot modular monolith.
- The frontend is React and TypeScript with Vite.
- PostgreSQL and Flyway provide persistence and migrations.
- Browser authentication uses server-side sessions and CSRF protection.
- Issued receipt content is immutable and signed with Ed25519.
- Public verification and private acknowledgement use separate links.
- Link delivery is manual copy-and-share; real email is outside P0.
- Acknowledgement, revocation, and moderation remain outside the signed payload.
- Docker Compose is the release runtime; public deployment is not required.

## Risk register

| Risk                                           | Planned mitigation                                     | Phase   |
| ---------------------------------------------- | ------------------------------------------------------ | ------- |
| Canonical payload differs across code paths    | One serializer plus golden vectors                     | 2–3     |
| Private acknowledgement link leaks             | Separate UI, hash at rest, no logs/referrer            | 3–5     |
| State transition race creates duplicate action | Locking, unique constraints, transaction tests         | 3–5     |
| Moderation rewrites signed history             | Separate moderation aggregate and database permissions | 3–4     |
| Scope expands into a social platform           | P0 and non-goals remain release contract               | All     |
| Local setup becomes fragile                    | Compose health checks and fresh-clone rehearsal        | 2 and 6 |
| Portfolio claims exceed implementation         | Acceptance evidence and honest status labels           | 7       |

## Phase 2 entry checklist

- [x] No unresolved product decision blocks implementation.
- [x] Critical user journeys and failure states are documented.
- [x] Domain transitions and invariants are explicit.
- [x] Security boundaries and secret-handling rules are explicit.
- [x] P0 exclusions protect the low-effort delivery target.
- [x] Local-only release status is explicit.
- [ ] Current supported dependency versions are verified.
- [ ] Repository and CI foundation are created.
- [ ] Initial application skeletons compile and test.

The remaining unchecked items are Phase 2 work.

## Phase 2 objective

Create the GitHub repository and executable foundation without implementing
product features yet:

1. initialize the monorepo and professional GitHub metadata;
2. lock compatible toolchain and dependency versions;
3. create backend and frontend skeletons with module boundaries;
4. add PostgreSQL, Flyway, and Compose health checks;
5. add formatters, linters, tests, CI, secret scanning, and line-limit checks;
6. publish a precise setup guide and first green commit.
