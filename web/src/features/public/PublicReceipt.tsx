import { useEffect, useState, type FormEvent } from 'react';
import { api, ApiError } from '../../shared/api';
import type { Session, Verification } from '../../shared/types';
import { Notice, Shell, Spinner } from '../../shared/ui';
import { VerificationReceipt } from '../receipts/ReceiptPreview';

export function PublicReceipt({
  publicId,
  session,
}: {
  publicId: string;
  session: Session | null;
}) {
  const [receipt, setReceipt] = useState<Verification | null>(null);
  const [error, setError] = useState('');
  const [reported, setReported] = useState(false);
  const [showReport, setShowReport] = useState(false);

  useEffect(() => {
    void api
      .verify(publicId)
      .then(setReceipt)
      .catch((cause) =>
        setError(
          cause instanceof ApiError ? cause.message : 'Receipt unavailable.',
        ),
      );
  }, [publicId]);

  async function report(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const values = new FormData(event.currentTarget);
    try {
      await api.report(
        publicId,
        String(values.get('reason')),
        String(values.get('details')),
      );
      setReported(true);
      setShowReport(false);
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'Report could not be submitted.',
      );
    }
  }

  return (
    <Shell
      actions={
        <a className="secondary button-link small" href="/">
          {session?.authenticated ? 'My receipts' : 'Open TXU'}
        </a>
      }
    >
      <section className="public-page page-section">
        {!receipt && !error && <Spinner label="Verifying signed receipt" />}
        {error && <Notice tone="danger">{error}</Notice>}
        {receipt && (
          <>
            <div className="verify-banner">
              <span className="verify-icon">
                {receipt.integrity === 'VALID' ? '✓' : '!'}
              </span>
              <div>
                <b>
                  {receipt.integrity === 'VALID'
                    ? 'Cryptographic signature verified'
                    : 'Verification needs attention'}
                </b>
                <p>
                  TXU rebuilt the immutable payload and checked it against the
                  recorded Ed25519 signature.
                </p>
              </div>
            </div>
            <VerificationReceipt receipt={receipt} />
            {reported ? (
              <Notice tone="success">
                Report received. A moderator can now review this receipt.
              </Notice>
            ) : (
              receipt.result !== 'HIDDEN' && (
                <button
                  className="text-button report-trigger"
                  onClick={() => setShowReport(true)}
                >
                  Report this receipt
                </button>
              )
            )}
          </>
        )}
      </section>
      {showReport && (
        <div className="modal-backdrop">
          <form className="modal card" onSubmit={(event) => void report(event)}>
            <p className="eyebrow">Private report</p>
            <h2>Tell a moderator what is wrong.</h2>
            <label>
              Reason
              <select name="reason">
                <option>HARASSMENT</option>
                <option>PERSONAL_INFORMATION</option>
                <option>IMPERSONATION</option>
                <option>OTHER</option>
              </select>
            </label>
            <label>
              Context
              <textarea name="details" maxLength={500} rows={4} />
            </label>
            <div className="form-actions">
              <button
                type="button"
                className="secondary"
                onClick={() => setShowReport(false)}
              >
                Cancel
              </button>
              <button className="primary">Submit report</button>
            </div>
          </form>
        </div>
      )}
    </Shell>
  );
}
