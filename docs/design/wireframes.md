# Low-Fidelity Wireframes

These wireframes define content hierarchy and states. They are not a visual
design contract. Phase 3 may refine layout while preserving every required
action and message.

## Shared interface principles

- The receipt itself is the visual focal point.
- Integrity and lifecycle are written in plain language before technical data.
- Public and private links never share one copy button.
- Success, warning, and error messages align content centrally within their
  capsule or panel and never rely on color alone.
- Mobile layouts preserve reading order and place the primary action within easy
  reach.
- Irreversible actions state the consequence before confirmation.

## 1. Landing page

```text
┌──────────────────────────────────────────────────────────────┐
│ TXU                                      Sign in  Get started │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  A thank-you can be copied.                                  │
│  Proof deserves a receipt.                                   │
│                                                              │
│  Issue signed appreciation that anyone with the link         │
│  can verify.                                                  │
│                                                              │
│  [Create a receipt]   [Verify a receipt]                     │
│                                                              │
│  How it works: Write → Preview → Sign → Share → Acknowledge  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## 2. Member dashboard

```text
┌──────────────────────────────────────────────────────────────┐
│ TXU                My receipts       Ari ▾                   │
├──────────────────────────────────────────────────────────────┤
│ Your appreciation receipts              [New receipt]        │
│                                                              │
│ [All] [Drafts] [Issued] [Acknowledged] [Revoked]             │
│                                                              │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ For keeping the release calm              ACKNOWLEDGED   │ │
│ │ To Maya · Teamwork · Issued 9 Oct 2026                   │ │
│ │ [Open]                                                   │ │
│ └──────────────────────────────────────────────────────────┘ │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ A careful review                              DRAFT      │ │
│ │ To Niko · Updated 8 Oct 2026                              │ │
│ │ [Continue writing]                                       │ │
│ └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

Empty state:

```text
You have not written a receipt yet.
Start with one contribution worth remembering.  [New receipt]
```

## 3. Receipt composer

```text
┌──────────────────────────────────────────────────────────────┐
│ New appreciation receipt                                    │
├──────────────────────────────────────────────────────────────┤
│ Recipient display name                                      │
│ [ Maya                                                   ]   │
│                                                              │
│ Title                                                        │
│ [ For keeping the release calm                           ]   │
│                                                              │
│ Category                                                     │
│ [ Teamwork                                              ▾ ]   │
│                                                              │
│ Message                                         76 / 1000    │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ You traced the failure, explained it clearly, and       │ │
│ │ helped us ship.                                         │ │
│ └──────────────────────────────────────────────────────────┘ │
│                                                              │
│ [Save draft]                                  [Preview →]    │
└──────────────────────────────────────────────────────────────┘
```

Validation appears directly below its field and the error summary links to the
first invalid input.

## 4. Immutable preview

```text
┌──────────────────────────────────────────────────────────────┐
│ Review before issuing                                       │
│ Once issued, these words cannot be edited or deleted.        │
├──────────────────────────────────────────────────────────────┤
│                     ┌──────────────────────┐                 │
│                     │ TXU RECEIPT          │                 │
│                     │ Thank you, Maya.     │                 │
│                     │                      │                 │
│                     │ For keeping the      │                 │
│                     │ release calm         │                 │
│                     │                      │                 │
│                     │ You traced...        │                 │
│                     │                      │                 │
│                     │ From Ari · Teamwork  │                 │
│                     └──────────────────────┘                 │
│                                                              │
│ [← Edit draft]                            [Issue receipt]    │
└──────────────────────────────────────────────────────────────┘
```

The issue button opens a final confirmation explaining permanence and link
privacy.

## 5. Issue success and link handoff

```text
┌──────────────────────────────────────────────────────────────┐
│                      ✓ Receipt issued                         │
│                The signed record is ready.                   │
├──────────────────────────────────────────────────────────────┤
│ Public verification link                                    │
│ Anyone with this link can read and verify the receipt.       │
│ [ txu.local/r/7Nx...                               ] [Copy]  │
│                                                              │
│ Private recipient link                                      │
│ This link can acknowledge once. Share it only with Maya.     │
│ [ txu.local/a/4Fg...                               ] [Copy]  │
│ This private link will not be shown again.                   │
│                                                              │
│ [Open public receipt]                  [Return to dashboard] │
└──────────────────────────────────────────────────────────────┘
```

Each copy button displays its own centered `Copied` feedback. The private link
is never included in the public-page DOM.

## 6. Public verification page

```text
┌──────────────────────────────────────────────────────────────┐
│ TXU                                                   Report │
├──────────────────────────────────────────────────────────────┤
│                 ✓ VERIFIED                                  │
│        The signed content matches the original issue.        │
│                                                              │
│                     ┌──────────────────────┐                 │
│                     │ Thank you, Maya.     │                 │
│                     │ For keeping the      │                 │
│                     │ release calm         │                 │
│                     │ You traced...        │                 │
│                     │ From Ari             │                 │
│                     └──────────────────────┘                 │
│                                                              │
│ Issued 9 Oct 2026 · Awaiting acknowledgement                 │
│                                                              │
│ Verification details ▾                                      │
│ Key ID · Algorithm · Payload fingerprint                     │
└──────────────────────────────────────────────────────────────┘
```

State variants:

- `VERIFIED + ACKNOWLEDGED`: green confirmation and acknowledgement time.
- `REVOKED`: red status; signed origin remains visible with revocation reason.
- `INVALID`: strong integrity warning; no positive verified language.
- `HIDDEN`: content replaced with a neutral moderation notice.
- `NOT_FOUND`: generic missing-page guidance.

## 7. Recipient acknowledgement

```text
┌──────────────────────────────────────────────────────────────┐
│ A receipt was shared with you                                │
├──────────────────────────────────────────────────────────────┤
│ ✓ Signature verified                                        │
│                                                              │
│ [Receipt content and issuer]                                 │
│                                                              │
│ Acknowledging confirms that you received this appreciation.  │
│ It does not verify your legal identity.                      │
│                                                              │
│                         [Acknowledge receipt]                 │
└──────────────────────────────────────────────────────────────┘
```

Used-token state:

```text
✓ Already acknowledged
This private link has completed its one-time action.
[View public receipt]
```

## 8. Member receipt detail

```text
┌──────────────────────────────────────────────────────────────┐
│ ← My receipts                         VERIFIED · ACKNOWLEDGED │
├──────────────────────────────────────────────────────────────┤
│ [Receipt preview]                                            │
│                                                              │
│ Activity                                                     │
│ ● Issued by you                              9 Oct, 10:12    │
│ ● Acknowledged                               9 Oct, 10:18    │
│                                                              │
│ Public link                                      [Copy]      │
│ Private recipient link                     No longer shown    │
│                                                              │
│ Danger zone                                                  │
│ Mark this receipt unusable while preserving its history.     │
│ [Revoke receipt]                                             │
└──────────────────────────────────────────────────────────────┘
```

## 9. Revocation confirmation

```text
┌──────────────────────────────────────────────────────────────┐
│ Revoke this receipt?                                         │
│ This cannot be undone. The signed content stays available    │
│ with a revoked status.                                       │
│                                                              │
│ Reason                                                       │
│ [ Issued to the wrong recipient                          ]   │
│                                                              │
│ [Cancel]                                  [Revoke receipt]   │
└──────────────────────────────────────────────────────────────┘
```

## 10. Report dialog

```text
┌──────────────────────────────────────────────────────────────┐
│ Report this receipt                                          │
│                                                              │
│ Reason                                                       │
│ ( ) Harassment or abuse                                      │
│ ( ) Personal information                                    │
│ ( ) Impersonation                                            │
│ ( ) Other                                                    │
│                                                              │
│ Details [ optional, bounded text                         ]   │
│                                                              │
│ [Cancel]                                     [Send report]   │
└──────────────────────────────────────────────────────────────┘
```

## 11. Moderator queue

```text
┌──────────────────────────────────────────────────────────────┐
│ Moderation                              Open reports: 3       │
├───────────────────────────┬──────────────────────────────────┤
│ OPEN REPORTS              │ Receipt review                   │
│                           │                                  │
│ ● Impersonation           │ [Visible receipt snapshot]       │
│   TXU-7Nx...              │                                  │
│                           │ Report details                   │
│ ○ Personal information    │ Prior actions                    │
│   TXU-2Km...              │ Signature status                │
│                           │                                  │
│                           │ Decision reason [             ]  │
│                           │ [Dismiss] [Hide receipt]         │
└───────────────────────────┴──────────────────────────────────┘
```

On narrow screens, the list and detail become separate routes to preserve
reading order and avoid compressed controls.

## Responsive checkpoints

| Width   | Required behavior                                          |
| ------- | ---------------------------------------------------------- |
| 360 px  | Single column, full-width actions, no horizontal scrolling |
| 768 px  | Comfortable form width and stacked moderation navigation   |
| 1280 px | Centered receipt canvas and split moderation workspace     |

## Screenshot plan for the final portfolio

Capture only after flows are validated:

1. landing narrative;
2. dashboard with mixed states;
3. composer validation;
4. immutable preview;
5. issue success with safely staged fictional links;
6. verified public receipt;
7. acknowledged receipt;
8. revoked receipt;
9. invalid integrity state using a controlled test fixture;
10. recipient acknowledgement flow;
11. report submission; and
12. moderator queue and decision result.
