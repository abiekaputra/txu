# Product Brief

## The story TXU tells

A useful contribution is often remembered in a chat message, a meeting, or a
sentence that disappears in a crowded timeline. Screenshots preserve the words,
but they do not prove which record is original or whether it was changed.

TXU gives appreciation a small, durable structure. The sender composes a
receipt, sees exactly what will become permanent, then issues it. TXU signs that
snapshot. The recipient can acknowledge it, and anyone with the public link can
check whether the signed content is intact and whether the receipt remains
valid.

TXU is pronounced **thank you**. Its visual direction is a folded receipt or
ticket monogram: compact, recognizable, and related to proof without imitating
a financial product.

## Problem statement

People can send appreciation quickly, but common channels provide weak
provenance and poor long-term retrieval. A portfolio-grade solution needs to
show that the message, issuer, and issue time belong to one immutable record,
while still supporting acknowledgement, revocation, and moderation.

## Product proposition

TXU is a local-first web product that creates cryptographically signed,
shareable appreciation receipts. It combines a human interaction with a clear
technical property: issued content can be verified and cannot be silently
edited.

TXU is a portfolio product and does not claim legal identity verification,
notarization, or legally binding electronic signatures.

## Users

| Persona         | Need                                        | Primary outcome                               |
| --------------- | ------------------------------------------- | --------------------------------------------- |
| Member / sender | Recognize a person's contribution           | Issues and shares a signed receipt            |
| Recipient       | Confirm that the appreciation reached them  | Acknowledges the receipt once                 |
| Public visitor  | Judge whether a shared receipt is authentic | Sees a plain-language verification result     |
| Moderator       | Respond to abusive or inappropriate content | Reviews reports and hides or restores records |
| Administrator   | Operate the local product safely            | Manages privileged access and audits actions  |

## Value proposition

- **For the sender:** a deliberate preview prevents accidental permanent
  wording, and the issued record is easy to share.
- **For the recipient:** acknowledgement is explicit and does not require a
  public social profile.
- **For a viewer:** verification explains integrity and operational status in
  ordinary language.
- **For an engineering reviewer:** the product demonstrates domain modeling,
  cryptographic signing, authorization, moderation, auditability, testing, and
  end-to-end delivery in a focused system.

## Goals

1. Make the complete create-to-verify journey usable through a browser.
2. Keep issued receipt content immutable and cryptographically verifiable.
3. Separate immutable proof from mutable operational states such as
   acknowledgement, revocation, and moderation.
4. Make privileged actions attributable through append-only audit events.
5. Run the product locally with one documented startup path.
6. Produce implementation-backed portfolio evidence without inflated claims.

## Success measures

These measures are release gates for a local portfolio product, rather than
unsupported market adoption forecasts.

| Measure                                      | Target                                              |
| -------------------------------------------- | --------------------------------------------------- |
| Critical browser journeys                    | 100% pass in Playwright                             |
| Issued receipts with valid server signatures | 100%                                                |
| Unauthorized mutation scenarios              | 100% rejected in integration tests                  |
| Fresh local setup                            | At most 10 minutes after dependencies are available |
| Public verification response                 | p95 below 500 ms in the documented local profile    |
| Accessibility                                | No serious automated violations on critical screens |
| Signed content mutation after issue          | Impossible through application APIs                 |
| Privileged mutations with audit events       | 100%                                                |

## Scope

### P0 — Portfolio release

- Local registration, login, logout, and secure session lifecycle
- Member dashboard with draft and issued receipts
- Draft creation, editing, preview, and deletion
- Receipt fields: recipient display name, title, message, and category
- Explicit issue confirmation
- Ed25519 signature over a canonical immutable payload
- Unguessable public verification URL
- Separate private, single-use acknowledgement URL
- Verification result with integrity and lifecycle status
- One-time recipient acknowledgement
- Sender revocation with reason
- Report submission from the public receipt page
- Moderator report queue, hide, dismiss, and restore actions
- Append-only audit events for security-sensitive actions
- Health checks, structured logs, and basic application metrics
- Responsive browser interface and documented local startup

### P1 — Useful extension

- Rotate an unused acknowledgement secret
- Export the signed verification envelope as JSON
- Search and filters for a member's own receipts
- Administrative role management interface
- Additional moderation analytics

### P2 — Future exploration

- Optional account-bound recipient claim
- Optional real email delivery
- Independent browser-side or CLI signature verification
- Organization workspaces and branded receipt templates
- Public discovery with explicit opt-in

## Non-goals for P0

- Native mobile application
- Social feed, follows, likes, or comments
- Chat or direct messaging
- File or media attachments
- Payment, rewards, or token economics
- Blockchain or distributed ledger
- Legal identity proof or legal electronic signature claims
- Third-party OAuth login
- Real email or push notification delivery
- Public cloud deployment
- Multi-tenant organization administration
- User-defined visual themes

## Product rules

1. A draft may change; an issued receipt's signed fields may not.
2. Issuing creates both a public verification link and a private acknowledgement
   secret. The private secret is displayed only when created.
3. A recipient does not need an account to verify or acknowledge a receipt.
4. Acknowledgement is idempotent and can occur only once while a receipt is
   issued.
5. Revocation is terminal. It changes the operational result but preserves the
   original signed payload and any earlier acknowledgement.
6. Moderation can hide a receipt from ordinary public viewing without deleting
   the signed record.
7. Reports and privileged actions never rewrite signed content.
8. Public receipt URLs are unlisted and use high-entropy identifiers; TXU has no
   public searchable gallery in P0.

## Assumptions resolved for implementation

- A copied link is the delivery mechanism because real email is out of scope.
- The sender receives a public link and a distinct private recipient link.
- Recipient identity is a sender-provided display label, not verified identity.
- The first release uses one configured active signing key and supports a key
  identifier so historical verification can survive later rotation.
- Categories are a controlled enum: `MENTORSHIP`, `TEAMWORK`, `SUPPORT`,
  `CRAFT`, and `OTHER`.
- English is the initial interface language; internationalization is deferred.
- Local Docker Compose is the reference runtime. Public hosting is optional
  future work.

No unresolved product decision blocks Phase 2.
