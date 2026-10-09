import { useState, type FormEvent } from 'react';
import { api, ApiError } from '../../shared/api';
import type { Session } from '../../shared/types';
import { Notice, Shell } from '../../shared/ui';

export function AuthPanel({
  onAuthenticated,
  sessionFailure = '',
}: {
  onAuthenticated: (session: Session) => void;
  sessionFailure?: string;
}) {
  const [mode, setMode] = useState<'register' | 'login'>('register');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(sessionFailure);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError('');
    const values = new FormData(event.currentTarget);
    const email = String(values.get('email'));
    const password = String(values.get('password'));
    try {
      const session =
        mode === 'register'
          ? await api.register({
              displayName: String(values.get('displayName')),
              email,
              password,
            })
          : await api.login({ email, password });
      onAuthenticated(session);
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'TXU could not sign you in.',
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <Shell>
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">A small proof of a meaningful contribution</p>
          <h1>Make your thank-you last longer than the timeline.</h1>
          <p className="lede">
            TXU turns appreciation into an immutable, signed receipt. Write it
            deliberately, share it privately, and let anyone verify that the
            words are still original.
          </p>
          <div className="trust-row">
            <span>Ed25519 signed</span>
            <span>Publicly verifiable</span>
            <span>Recipient acknowledged</span>
          </div>
        </div>
        <div className="auth-card card">
          <div className="tabs" role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'register'}
              className={mode === 'register' ? 'active' : ''}
              onClick={() => setMode('register')}
            >
              Create account
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'login'}
              className={mode === 'login' ? 'active' : ''}
              onClick={() => setMode('login')}
            >
              Sign in
            </button>
          </div>
          <form onSubmit={submit}>
            {mode === 'register' && (
              <label>
                Display name
                <input
                  name="displayName"
                  minLength={2}
                  maxLength={80}
                  required
                  autoComplete="name"
                />
              </label>
            )}
            <label>
              Email
              <input name="email" type="email" required autoComplete="email" />
            </label>
            <label>
              Password
              <input
                name="password"
                type="password"
                minLength={12}
                required
                autoComplete={
                  mode === 'register' ? 'new-password' : 'current-password'
                }
              />
            </label>
            {error && <Notice tone="danger">{error}</Notice>}
            <button className="primary wide" disabled={busy}>
              {busy
                ? 'Working…'
                : mode === 'register'
                  ? 'Begin with TXU'
                  : 'Open my receipts'}
            </button>
          </form>
          <p className="fine-print">
            Local portfolio release. No marketing email and no public directory.
          </p>
        </div>
      </section>
      <section className="story-grid page-section">
        <article>
          <b>01</b>
          <h2>Write with intent</h2>
          <p>
            A draft stays editable until you confirm the exact permanent
            wording.
          </p>
        </article>
        <article>
          <b>02</b>
          <h2>Issue the proof</h2>
          <p>
            TXU signs one canonical snapshot and protects it from silent
            changes.
          </p>
        </article>
        <article>
          <b>03</b>
          <h2>Close the loop</h2>
          <p>
            The recipient acknowledges privately while the public link remains
            independently verifiable.
          </p>
        </article>
      </section>
    </Shell>
  );
}
