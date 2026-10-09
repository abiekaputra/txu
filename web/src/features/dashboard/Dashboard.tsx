import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../../shared/api';
import type { Receipt, Session } from '../../shared/types';
import { Notice, Shell, Spinner } from '../../shared/ui';
import { navigate } from '../../app/use-route';
import { Composer } from '../receipts/Composer';
import { MemberReceipt } from '../receipts/MemberReceipt';
import { ReceiptCard } from './ReceiptCard';

type View =
  | { kind: 'list' }
  | { kind: 'compose'; receipt?: Receipt }
  | { kind: 'detail'; receipt: Receipt };

export function Dashboard({
  session,
  onLogout,
}: {
  session: Session;
  onLogout: (session: Session) => void;
}) {
  const [receipts, setReceipts] = useState<Receipt[] | null>(null);
  const [view, setView] = useState<View>({ kind: 'list' });
  const [error, setError] = useState('');
  const user = session.user!;

  const refresh = useCallback(async () => {
    try {
      setReceipts(await api.listReceipts());
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'Receipts could not be loaded.',
      );
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const actions = (
    <>
      {(user.role === 'MODERATOR' || user.role === 'ADMIN') && (
        <button className="text-button" onClick={() => navigate('/moderation')}>
          Moderation
        </button>
      )}
      <span className="user-chip">{user.displayName}</span>
      <button
        className="secondary small"
        onClick={() => void api.logout().then(onLogout)}
      >
        Sign out
      </button>
    </>
  );

  if (view.kind === 'compose')
    return (
      <Shell actions={actions}>
        <Composer
          receipt={view.receipt}
          onClose={() => {
            setView({ kind: 'list' });
            void refresh();
          }}
        />
      </Shell>
    );
  if (view.kind === 'detail')
    return (
      <Shell actions={actions}>
        <MemberReceipt
          initial={view.receipt}
          onBack={() => {
            setView({ kind: 'list' });
            void refresh();
          }}
        />
      </Shell>
    );

  return (
    <Shell actions={actions}>
      <section className="dashboard page-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Your receipt desk</p>
            <h1>Appreciation worth keeping.</h1>
            <p>Draft carefully. Issue once. Verify anytime.</p>
          </div>
          <button
            className="primary"
            onClick={() => setView({ kind: 'compose' })}
          >
            + New receipt
          </button>
        </div>
        {error && <Notice tone="danger">{error}</Notice>}
        {!receipts ? (
          <Spinner label="Loading your receipts" />
        ) : receipts.length === 0 ? (
          <div className="empty-state card">
            <div className="empty-mark">TXU</div>
            <h2>Your first thank-you starts here.</h2>
            <p>
              Recognize a contribution, preview the permanent record, then issue
              it when every word feels right.
            </p>
            <button
              className="primary"
              onClick={() => setView({ kind: 'compose' })}
            >
              Create a receipt
            </button>
          </div>
        ) : (
          <div className="receipt-grid">
            {receipts.map((receipt) => (
              <ReceiptCard
                key={receipt.id}
                receipt={receipt}
                onOpen={() =>
                  setView(
                    receipt.lifecycle === 'DRAFT'
                      ? { kind: 'compose', receipt }
                      : { kind: 'detail', receipt },
                  )
                }
              />
            ))}
          </div>
        )}
      </section>
    </Shell>
  );
}
