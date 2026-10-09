import { useEffect, useState } from 'react';
import { api, ApiError } from '../../shared/api';
import type { Acknowledgement } from '../../shared/types';
import { Notice, Shell, Spinner, Status } from '../../shared/ui';
import { VerificationReceipt } from '../receipts/ReceiptPreview';

export function AcknowledgementPage({ token }: { token: string }) {
  const [acknowledgement, setAcknowledgement] =
    useState<Acknowledgement | null>(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    void api
      .acknowledgement(token)
      .then(setAcknowledgement)
      .catch((cause) =>
        setError(
          cause instanceof ApiError
            ? cause.message
            : 'This private link is unavailable.',
        ),
      );
  }, [token]);

  async function acknowledge() {
    setBusy(true);
    setError('');
    try {
      setAcknowledgement(await api.acknowledge(token));
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'Acknowledgement could not be recorded.',
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <Shell>
      <section className="ack-page page-section">
        {!acknowledgement && !error && (
          <Spinner label="Opening private receipt" />
        )}
        {error && <Notice tone="danger">{error}</Notice>}
        {acknowledgement && (
          <>
            <div className="ack-intro">
              <p className="eyebrow">A thank-you reached you</p>
              <h1>Acknowledge the contribution behind this receipt.</h1>
              <p>
                This private link records one acknowledgement. It never changes
                the sender’s signed message.
              </p>
              <Status
                tone={
                  acknowledgement.state === 'ACKNOWLEDGED'
                    ? 'success'
                    : 'warning'
                }
              >
                {acknowledgement.state}
              </Status>
            </div>
            <VerificationReceipt receipt={acknowledgement.receipt} />
            {acknowledgement.eligible ? (
              <button
                className="primary ack-button"
                disabled={busy}
                onClick={() => void acknowledge()}
              >
                {busy ? 'Recording…' : 'Acknowledge receipt'}
              </button>
            ) : (
              acknowledgement.state === 'ACKNOWLEDGED' && (
                <Notice tone="success">
                  Acknowledgement recorded. This private action is complete.
                </Notice>
              )
            )}
          </>
        )}
      </section>
    </Shell>
  );
}
