import { useState, type FormEvent } from 'react';
import { api, ApiError } from '../../shared/api';
import type {
  Category,
  DraftInput,
  IssueResult,
  Receipt,
} from '../../shared/types';
import { copyText } from '../../shared/presentation';
import { Notice } from '../../shared/ui';
import { ReceiptPreview } from './ReceiptPreview';

const categories: Category[] = [
  'MENTORSHIP',
  'TEAMWORK',
  'SUPPORT',
  'CRAFT',
  'OTHER',
];
const empty: DraftInput = {
  recipientLabel: '',
  title: '',
  message: '',
  category: 'TEAMWORK',
};

export function Composer({
  receipt,
  onClose,
}: {
  receipt?: Receipt;
  onClose: () => void;
}) {
  const [draft, setDraft] = useState<DraftInput>(
    receipt
      ? {
          recipientLabel: receipt.recipientLabel,
          title: receipt.title,
          message: receipt.message,
          category: receipt.category,
        }
      : empty,
  );
  const [saved, setSaved] = useState<Receipt | undefined>(receipt);
  const [issued, setIssued] = useState<IssueResult | null>(null);
  const [step, setStep] = useState<'write' | 'preview'>('write');
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function change(name: keyof DraftInput, value: string) {
    setDraft((current) => ({ ...current, [name]: value }));
    setNotice('');
  }

  async function save(event?: FormEvent) {
    event?.preventDefault();
    setBusy(true);
    setError('');
    try {
      const next = saved
        ? await api.updateReceipt(saved.id, draft)
        : await api.createReceipt(draft);
      setSaved(next);
      setNotice('Draft saved. You can still change every field.');
      return next;
    } catch (cause) {
      setError(
        cause instanceof ApiError ? cause.message : 'Draft could not be saved.',
      );
    } finally {
      setBusy(false);
    }
  }

  async function issue() {
    setBusy(true);
    setError('');
    try {
      const current = saved ?? (await save());
      if (!current) return;
      setIssued(await api.issueReceipt(current.id));
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'Receipt could not be issued.',
      );
    } finally {
      setBusy(false);
    }
  }

  if (issued)
    return (
      <section className="success-page page-section">
        <div className="success-orbit">✓</div>
        <p className="eyebrow">Receipt issued</p>
        <h1>Your appreciation now has a verifiable history.</h1>
        <p>
          The public link can be shared widely. The recipient link is private
          and shown only now, so save it before leaving.
        </p>
        <LinkBox label="Public verification link" value={issued.publicUrl} />
        {issued.recipientUrl && (
          <LinkBox
            label="Private one-time recipient link"
            value={issued.recipientUrl}
            privateLink
          />
        )}
        <button className="primary" onClick={onClose}>
          Return to receipts
        </button>
      </section>
    );

  return (
    <section className="composer page-section">
      <div className="composer-head">
        <button className="back" onClick={onClose}>
          ← Receipts
        </button>
        <span>Draft · editable</span>
      </div>
      <div className="composer-grid">
        <div>
          <p className="eyebrow">
            {step === 'write' ? 'Write with intent' : 'Final review'}
          </p>
          <h1>
            {step === 'write'
              ? 'Who made a difference?'
              : 'These words become permanent.'}
          </h1>
          {step === 'write' ? (
            <form onSubmit={(event) => void save(event)}>
              <label>
                Recipient name
                <input
                  value={draft.recipientLabel}
                  onChange={(event) =>
                    change('recipientLabel', event.target.value)
                  }
                  minLength={2}
                  maxLength={80}
                  required
                />
              </label>
              <label>
                Receipt title
                <input
                  value={draft.title}
                  onChange={(event) => change('title', event.target.value)}
                  minLength={5}
                  maxLength={80}
                  required
                />
              </label>
              <label>
                Category
                <select
                  value={draft.category}
                  onChange={(event) => change('category', event.target.value)}
                >
                  {categories.map((category) => (
                    <option key={category}>{category}</option>
                  ))}
                </select>
              </label>
              <label>
                What did they contribute?
                <textarea
                  value={draft.message}
                  onChange={(event) => change('message', event.target.value)}
                  minLength={20}
                  maxLength={1000}
                  rows={7}
                  required
                />
                <small>{draft.message.length} / 1000</small>
              </label>
              {notice && <Notice tone="success">{notice}</Notice>}
              {error && <Notice tone="danger">{error}</Notice>}
              <div className="form-actions">
                <button className="secondary" disabled={busy}>
                  Save draft
                </button>
                <button
                  type="button"
                  className="primary"
                  onClick={() => setStep('preview')}
                  disabled={
                    !draft.recipientLabel ||
                    draft.title.length < 5 ||
                    draft.message.length < 20
                  }
                >
                  Preview receipt
                </button>
              </div>
            </form>
          ) : (
            <div className="confirm-panel">
              <p>
                Issuing signs this exact recipient, title, message, category,
                issuer, and timestamp. The signed fields cannot be edited later.
              </p>
              {error && <Notice tone="danger">{error}</Notice>}
              <div className="form-actions">
                <button className="secondary" onClick={() => setStep('write')}>
                  Keep editing
                </button>
                <button
                  className="primary"
                  disabled={busy}
                  onClick={() => void issue()}
                >
                  {busy ? 'Signing…' : 'Confirm and issue'}
                </button>
              </div>
            </div>
          )}
        </div>
        <ReceiptPreview draft={draft} />
      </div>
    </section>
  );
}

function LinkBox({
  label,
  value,
  privateLink = false,
}: {
  label: string;
  value: string;
  privateLink?: boolean;
}) {
  const [copied, setCopied] = useState(false);
  return (
    <div className={`link-box ${privateLink ? 'private' : ''}`}>
      <div>
        <b>{label}</b>
        {privateLink && <span>Keep private</span>}
      </div>
      <code>{value}</code>
      <button
        onClick={() =>
          void copyText(value).then(() => {
            setCopied(true);
            setTimeout(() => setCopied(false), 1600);
          })
        }
      >
        {copied ? 'Copied' : 'Copy link'}
      </button>
    </div>
  );
}
