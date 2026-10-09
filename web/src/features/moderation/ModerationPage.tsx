import { useCallback, useEffect, useState } from 'react';
import { navigate } from '../../app/use-route';
import { api, ApiError } from '../../shared/api';
import type { Report, User } from '../../shared/types';
import { formatDate } from '../../shared/presentation';
import { Notice, Shell, Spinner, Status } from '../../shared/ui';

export function ModerationPage({ user }: { user: User }) {
  const [reports, setReports] = useState<Report[] | null>(null);
  const [selected, setSelected] = useState<Report | null>(null);
  const [reason, setReason] = useState(
    'Reviewed against the TXU community standard.',
  );
  const [error, setError] = useState('');
  const load = useCallback(
    () =>
      void api
        .reports()
        .then(setReports)
        .catch((cause) =>
          setError(
            cause instanceof ApiError
              ? cause.message
              : 'Queue could not be loaded.',
          ),
        ),
    [],
  );
  useEffect(load, [load]);

  async function decide(decision: 'DISMISS' | 'HIDE' | 'RESTORE') {
    if (!selected) return;
    try {
      await api.decide(selected.id, decision, reason);
      setSelected(null);
      load();
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'Decision could not be saved.',
      );
    }
  }

  return (
    <Shell
      actions={
        <>
          <span className="user-chip">{user.displayName}</span>
          <button className="secondary small" onClick={() => navigate('/')}>
            Member view
          </button>
        </>
      }
    >
      <section className="moderation page-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Trust operations</p>
            <h1>Moderation queue</h1>
            <p>Review reports without rewriting the signed receipt history.</p>
          </div>
          <Status tone="warning">
            {reports?.filter((report) => report.status === 'OPEN').length ?? 0}{' '}
            OPEN
          </Status>
        </div>
        {error && <Notice tone="danger">{error}</Notice>}
        {!reports ? (
          <Spinner />
        ) : reports.length === 0 ? (
          <div className="empty-state card">
            <h2>The queue is clear.</h2>
            <p>No receipt needs a moderation decision.</p>
          </div>
        ) : (
          <div className="report-list">
            {reports.map((report) => (
              <button
                key={report.id}
                className="report-row"
                onClick={() => setSelected(report)}
              >
                <div>
                  <Status
                    tone={report.status === 'OPEN' ? 'warning' : 'neutral'}
                  >
                    {report.status}
                  </Status>
                  <small>{report.reason}</small>
                </div>
                <strong>{report.receiptTitle}</strong>
                <span>
                  For {report.recipientLabel} · by {report.issuerLabel}
                </span>
                <time>{formatDate(report.createdAt)}</time>
              </button>
            ))}
          </div>
        )}
      </section>
      {selected && (
        <div className="modal-backdrop">
          <div className="modal card">
            <p className="eyebrow">Moderation decision</p>
            <h2>{selected.receiptTitle}</h2>
            <p>
              <b>{selected.reason}</b> ·{' '}
              {selected.details || 'No additional context supplied.'}
            </p>
            <a href={`/r/${selected.publicId}`} target="_blank">
              Inspect public receipt ↗
            </a>
            <label>
              Decision rationale
              <textarea
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                maxLength={240}
                rows={3}
              />
            </label>
            <div className="moderation-actions">
              <button className="secondary" onClick={() => setSelected(null)}>
                Cancel
              </button>
              <button
                className="secondary"
                onClick={() => void decide('DISMISS')}
              >
                Dismiss report
              </button>
              {selected.moderationState === 'HIDDEN' ? (
                <button
                  className="primary"
                  onClick={() => void decide('RESTORE')}
                >
                  Restore receipt
                </button>
              ) : (
                <button
                  className="danger-button"
                  onClick={() => void decide('HIDE')}
                >
                  Hide receipt
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </Shell>
  );
}
