# User Flows

## 1. Register and sign in

```mermaid
flowchart LR
    A[Landing page] --> B[Register]
    B --> C{Input valid?}
    C -- No --> D[Inline corrections]
    D --> B
    C -- Yes --> E[Account created]
    E --> F[Authenticated dashboard]
    A --> G[Sign in]
    G --> H{Credentials valid?}
    H -- No --> I[Safe error and retry]
    H -- Yes --> F
```

The server creates a secure browser session after successful authentication. A
generic credential error avoids revealing whether an account exists.

## 2. Create, preview, and issue a receipt

```mermaid
flowchart TD
    A[Dashboard] --> B[New receipt]
    B --> C[Enter recipient, title, category, message]
    C --> D{Valid?}
    D -- No --> C
    D -- Yes --> E[Save draft]
    E --> F[Preview immutable payload]
    F --> G{Ready to issue?}
    G -- No --> H[Edit draft]
    H --> F
    G -- Yes --> I[Confirm issue]
    I --> J[Canonicalize and sign in one transaction]
    J --> K{Issue successful?}
    K -- No --> L[Draft retained and safe error shown]
    K -- Yes --> M[Issued receipt]
    M --> N[Show public link]
    M --> O[Show private recipient link once]
```

The issue boundary is deliberate. Draft edits remain reversible. After issue,
the signed fields become immutable and the acknowledgement secret is never
stored in plaintext.

## 3. Public verification

```mermaid
flowchart TD
    A[Open public receipt URL] --> B{Record found?}
    B -- No --> C[Not found]
    B -- Yes --> D{Moderation hidden?}
    D -- Yes --> E[Limited hidden notice]
    D -- No --> F[Load signed envelope]
    F --> G[Rebuild canonical payload]
    G --> H[Verify Ed25519 signature]
    H --> I{Signature valid?}
    I -- No --> J[Invalid integrity warning]
    I -- Yes --> K{Lifecycle state}
    K -- Revoked --> L[Verified origin, revoked result]
    K -- Issued --> M{Acknowledged?}
    M -- No --> N[Verified result]
    M -- Yes --> O[Verified and acknowledged result]
```

Verification separates two questions:

1. Does the signature match the immutable issued payload?
2. Is the receipt currently usable according to lifecycle and moderation state?

A revoked receipt may retain a valid historical signature while its effective
result is `REVOKED`.

## 4. Recipient acknowledgement

```mermaid
flowchart TD
    A[Open private recipient URL] --> B[Hash supplied token]
    B --> C{Token matches?}
    C -- No --> D[Invalid or already used result]
    C -- Yes --> E{Receipt eligible?}
    E -- Revoked or hidden --> F[Acknowledgement unavailable]
    E -- Issued --> G[Show verified receipt and confirmation]
    G --> H[Recipient confirms]
    H --> I[Atomic one-time acknowledgement]
    I --> J[Token invalidated]
    J --> K[Acknowledged result]
```

The endpoint is idempotent at the business level: a replay cannot create a
second acknowledgement or recover the secret.

## 5. Sender revocation

```mermaid
flowchart LR
    A[Open own issued receipt] --> B[Choose revoke]
    B --> C[Enter reason]
    C --> D[Confirm irreversible action]
    D --> E{Authorized and still issued?}
    E -- No --> F[Safe conflict or forbidden result]
    E -- Yes --> G[Mark revoked and append audit event]
    G --> H[Public verification shows revoked]
```

Revocation is terminal. TXU retains the signed snapshot, acknowledgement event,
revocation reason, actor, and timestamp.

## 6. Report and moderation

```mermaid
flowchart TD
    A[Public receipt] --> B[Submit report reason]
    B --> C{Valid and within rate limit?}
    C -- No --> D[Validation or retry guidance]
    C -- Yes --> E[Open moderation report]
    E --> F[Moderator queue]
    F --> G[Inspect receipt, report, and history]
    G --> H{Decision}
    H -- Dismiss --> I[Close report and retain visibility]
    H -- Hide --> J[Hide public content and append audit event]
    J --> K[Optional later restore]
    K --> L[Restore visibility and append audit event]
```

Anonymous reporting stores a coarse abuse-prevention fingerprint rather than a
raw IP address. It is never part of the signed receipt.

## 7. Failure and recovery

| Failure                   | User-facing behavior                       | Data guarantee                      |
| ------------------------- | ------------------------------------------ | ----------------------------------- |
| Validation rejected       | Field-level guidance; form values retained | No invalid mutation                 |
| Session expired           | Sign-in prompt and safe return path        | No unauthorized mutation            |
| Database unavailable      | Retryable service message                  | No partial issue or acknowledgement |
| Signing key unavailable   | Issue blocked; draft retained              | No unsigned issued receipt          |
| Duplicate acknowledgement | Already used result                        | One acknowledgement event           |
| Concurrent revocation     | Current state returned                     | Terminal transition occurs once     |
| Unknown public ID         | Neutral not-found page                     | No identifier enumeration detail    |
| Invalid signature         | Prominent integrity warning                | Record preserved for investigation  |
