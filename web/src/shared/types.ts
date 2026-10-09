export type Role = 'MEMBER' | 'MODERATOR' | 'ADMIN';
export type Lifecycle = 'DRAFT' | 'ISSUED' | 'REVOKED';
export type ModerationState = 'CLEAR' | 'FLAGGED' | 'HIDDEN';
export type Category =
  'MENTORSHIP' | 'TEAMWORK' | 'SUPPORT' | 'CRAFT' | 'OTHER';

export interface User {
  id: string;
  email: string;
  displayName: string;
  role: Role;
}

export interface Session {
  authenticated: boolean;
  user: User | null;
  csrfToken: string;
}

export interface Receipt {
  id: string;
  recipientLabel: string;
  title: string;
  message: string;
  category: Category;
  lifecycle: Lifecycle;
  moderationState: ModerationState;
  publicId: string | null;
  issuedAt: string | null;
  acknowledgedAt: string | null;
  revokedAt: string | null;
  revocationReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DraftInput {
  recipientLabel: string;
  title: string;
  message: string;
  category: Category;
}

export interface IssueResult {
  receipt: Receipt;
  publicUrl: string;
  recipientUrl: string | null;
}

export interface Verification {
  result:
    'VERIFIED' | 'VERIFIED_ACKNOWLEDGED' | 'REVOKED' | 'INVALID' | 'HIDDEN';
  integrity: 'VALID' | 'INVALID' | 'NOT_DISCLOSED';
  lifecycle: Lifecycle | null;
  moderationState: ModerationState | null;
  acknowledgedAt: string | null;
  revokedAt: string | null;
  revocationReason: string | null;
  publicId: string | null;
  issuerLabel: string | null;
  recipientLabel: string | null;
  title: string | null;
  message: string | null;
  category: Category | null;
  issuedAt: string | null;
  keyId: string | null;
  payloadSha256: string | null;
}

export interface Acknowledgement {
  eligible: boolean;
  state: 'PENDING' | 'ACKNOWLEDGED' | 'UNAVAILABLE';
  receipt: Verification;
}

export interface Report {
  id: string;
  receiptId: string;
  publicId: string;
  receiptTitle: string;
  recipientLabel: string;
  issuerLabel: string;
  reason: string;
  details: string | null;
  status: 'OPEN' | 'DISMISSED' | 'RESOLVED';
  moderationState: ModerationState;
  decisionReason: string | null;
  createdAt: string;
  decidedAt: string | null;
}
