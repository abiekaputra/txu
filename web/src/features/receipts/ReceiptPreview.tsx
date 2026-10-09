import type { DraftInput, Verification } from '../../shared/types';
import { formatDate } from '../../shared/presentation';
import { Status } from '../../shared/ui';

export function ReceiptPreview({ draft }: { draft: DraftInput }) {
  return (
    <div className="receipt-paper">
      <div className="paper-header">
        <img src="/txu-mark.png" alt="" />
        <span>TXU appreciation receipt</span>
      </div>
      <p className="paper-for">Issued for</p>
      <h2>{draft.recipientLabel || 'Recipient name'}</h2>
      <h3>{draft.title || 'A title for the contribution'}</h3>
      <blockquote>
        {draft.message || 'Your appreciation message will appear here.'}
      </blockquote>
      <div className="paper-meta">
        <span>Category</span>
        <b>{draft.category}</b>
        <span>Issuer</span>
        <b>You</b>
      </div>
      <p className="paper-foot">
        This preview becomes immutable only after issue.
      </p>
    </div>
  );
}

export function VerificationReceipt({ receipt }: { receipt: Verification }) {
  const valid = receipt.integrity === 'VALID';
  const tone =
    receipt.result === 'REVOKED'
      ? 'danger'
      : receipt.result === 'HIDDEN'
        ? 'warning'
        : valid
          ? 'success'
          : 'danger';
  return (
    <article className="verification-card card">
      <div className="verification-heading">
        <div>
          <p className="eyebrow">Public verification</p>
          <h1>{receipt.title ?? 'Receipt unavailable'}</h1>
        </div>
        <Status tone={tone}>{receipt.result.replaceAll('_', ' ')}</Status>
      </div>
      {receipt.result === 'HIDDEN' ? (
        <p>
          This receipt is unavailable while a moderation decision is in effect.
        </p>
      ) : (
        <>
          <p className="receipt-to">
            For <strong>{receipt.recipientLabel}</strong>, from{' '}
            <strong>{receipt.issuerLabel}</strong>
          </p>
          <blockquote className="public-message">{receipt.message}</blockquote>
          <dl className="proof-grid">
            <div>
              <dt>Issued</dt>
              <dd>{formatDate(receipt.issuedAt)}</dd>
            </div>
            <div>
              <dt>Category</dt>
              <dd>{receipt.category}</dd>
            </div>
            <div>
              <dt>Integrity</dt>
              <dd>{receipt.integrity}</dd>
            </div>
            <div>
              <dt>Key</dt>
              <dd>{receipt.keyId}</dd>
            </div>
            <div className="hash">
              <dt>Payload fingerprint</dt>
              <dd>{receipt.payloadSha256}</dd>
            </div>
          </dl>
          {receipt.revocationReason && (
            <p className="revocation">
              <b>Revoked:</b> {receipt.revocationReason}
            </p>
          )}
        </>
      )}
    </article>
  );
}
