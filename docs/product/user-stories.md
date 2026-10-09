# User Stories

## Member and sender

### Identity

- As a visitor, I want to create a local account so that my receipts have an
  attributable issuer.
- As a member, I want to sign in and sign out so that only I can manage my
  drafts and issued receipts.
- As a member, I want expired or invalid sessions handled clearly so that I can
  recover without losing unsaved form content where practical.

### Drafting

- As a member, I want to create a receipt draft with a recipient display name,
  title, category, and message so that I can express specific appreciation.
- As a member, I want field-level validation before saving so that I understand
  how to correct invalid input.
- As a member, I want to edit or delete my draft so that unfinished wording is
  not permanent.
- As a member, I want a preview of the exact signed content so that I can review
  the permanent record before issuing it.

### Issuing and managing

- As a member, I want an explicit confirmation before issue so that I do not
  make a draft immutable by accident.
- As a member, I want TXU to sign the receipt atomically so that a successful
  issue never produces a partial record.
- As a member, I want the public and recipient links shown separately so that I
  do not accidentally publish the private acknowledgement capability.
- As a member, I want to copy the public link so that others can verify the
  receipt.
- As a member, I want to copy the private link so that the intended recipient
  can acknowledge it.
- As a member, I want to revoke my issued receipt with a reason so that a record
  can be marked unusable without deleting history.
- As a member, I want to see acknowledgement, revocation, and moderation status
  so that I know the current result.

## Recipient

- As a recipient with the private link, I want to see the receipt and its
  verification state before acting so that I know what I am acknowledging.
- As a recipient, I want to acknowledge the receipt once so that acceptance is
  explicit and durable.
- As a recipient, I want reused, invalid, or revoked links to return safe and
  understandable results.

## Public visitor

- As a visitor with a public link, I want to see the receipt's issuer label,
  recipient label, message, issue time, and status so that I understand it.
- As a visitor, I want a simple `Verified`, `Revoked`, `Hidden`, or `Invalid`
  result with supporting details so that I do not need cryptography knowledge.
- As a visitor, I want to see the signature algorithm, key identifier, and
  payload fingerprint so that technical proof remains inspectable.
- As a visitor, I want to report inappropriate content so that abuse can be
  reviewed without exposing my identity.

## Moderator

- As a moderator, I want a queue of open reports so that I can review potential
  abuse efficiently.
- As a moderator, I want to inspect the receipt, report reason, and prior
  actions so that decisions are contextual.
- As a moderator, I want to hide, dismiss, or restore a receipt with a required
  reason so that every moderation decision is attributable.

## Administrator

- As an administrator, I want privileged actions recorded as append-only audit
  events so that operational changes can be investigated.
- As an administrator, I want health and readiness information so that I can
  identify database or signing-key failures.
- As an administrator, I want privileged endpoints protected by roles so that a
  normal member cannot perform moderation or administration actions.

## Engineering reviewer

- As a reviewer, I want a one-command local runtime and seeded demo journey so
  that I can evaluate the product instead of reconstructing the environment.
- As a reviewer, I want tests around state transitions, signatures,
  authorization, and browser journeys so that the project's claims have
  evidence.
- As a reviewer, I want architecture and decision records aligned with the
  implementation so that the system remains explainable.
