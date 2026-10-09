import { useState } from 'react';
import { api, ApiError } from '../../shared/api';
import type { Receipt } from '../../shared/types';
import { formatDate } from '../../shared/presentation';
import { Notice, Status } from '../../shared/ui';

export function MemberReceipt({
  initial,
  onBack,
}: {
  initial: Receipt;
  onBack: () => void;
}) {
  const [receipt, setReceipt] = useState(initial);
  const [reason, setReason] = useState('');
  const [showRevoke, setShowRevoke] = useState(false);
  const [error, setError] = useState('');

  async function revoke() {
    try {
      setReceipt(await api.revokeReceipt(receipt.id, reason));
      setShowRevoke(false);
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'Receipt could not be revoked.',
      );
    }
  }

  return (
    <section className="detail-page page-section">
      <button className="back" onClick={onBack}>
        ← Receipts
      </button>
      <div className="detail-layout">
        <article className="receipt-paper large">
          <div className="paper-header">
            <img src="/txu-mark.png" alt="" />
            <span>TXU appreciation receipt</span>
          </div>
          <p className="paper-for">Issued for</p>
          <h2>{receipt.recipientLabel}</h2>
          <h3>{receipt.title}</h3>
          <blockquote>{receipt.message}</blockquote>
          <div className="paper-meta">
            <span>Category</span>
            <b>{receipt.category}</b>
            <span>Issued</span>
            <b>{formatDate(receipt.issuedAt)}</b>
          </div>
        </article>
        <aside className="detail-side card">
          <Status tone={receipt.lifecycle === 'REVOKED' ? 'danger' : 'success'}>
            {receipt.lifecycle}
          </Status>
          <h2>Receipt history</h2>
          <dl>
            <dt>Issued</dt>
            <dd>{formatDate(receipt.issuedAt)}</dd>
            <dt>Acknowledged</dt>
            <dd>{formatDate(receipt.acknowledgedAt)}</dd>
            <dt>Moderation</dt>
            <dd>{receipt.moderationState}</dd>
          </dl>
          {receipt.publicId && (
            <a
              className="secondary button-link"
              href={`/r/${receipt.publicId}`}
            >
              Open public proof
            </a>
          )}
          {receipt.lifecycle === 'ISSUED' && (
            <button
              className="danger-button"
              onClick={() => setShowRevoke(true)}
            >
              Revoke receipt
            </button>
          )}
          {receipt.revocationReason && (
            <Notice tone="danger">Revoked: {receipt.revocationReason}</Notice>
          )}
        </aside>
      </div>
      {showRevoke && (
        <div className="modal-backdrop">
          <div className="modal card" role="dialog" aria-modal="true">
            <p className="eyebrow">Irreversible action</p>
            <h2>Revoke this receipt?</h2>
            <p>
              The signed proof stays visible, but verification will clearly mark
              it revoked.
            </p>
            <label>
              Reason
              <textarea
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                rows={3}
                required
              />
            </label>
            {error && <Notice tone="danger">{error}</Notice>}
            <div className="form-actions">
              <button
                className="secondary"
                onClick={() => setShowRevoke(false)}
              >
                Cancel
              </button>
              <button
                className="danger-button"
                disabled={!reason.trim()}
                onClick={() => void revoke()}
              >
                Confirm revocation
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
