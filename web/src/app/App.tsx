import { useEffect, useState } from 'react';
import { AuthPanel } from '../features/auth/AuthPanel';
import { Dashboard } from '../features/dashboard/Dashboard';
import { ModerationPage } from '../features/moderation/ModerationPage';
import { AcknowledgementPage } from '../features/public/AcknowledgementPage';
import { PublicReceipt } from '../features/public/PublicReceipt';
import { loadSession } from '../shared/api';
import type { Session } from '../shared/types';
import { Shell, Spinner } from '../shared/ui';
import { navigate, useRoute } from './use-route';

export function App() {
  const path = useRoute();
  const [session, setSession] = useState<Session | null>(null);
  const [failure, setFailure] = useState('');

  useEffect(() => {
    void loadSession()
      .then(setSession)
      .catch(() =>
        setFailure(
          'The TXU service is unavailable. Check the local API and try again.',
        ),
      );
  }, []);

  if (!session && !failure)
    return (
      <Shell>
        <Spinner label="Opening TXU" />
      </Shell>
    );

  const publicId = path.match(/^\/r\/([^/]+)$/)?.[1];
  const acknowledgementToken = path.match(/^\/a\/([^/]+)$/)?.[1];
  if (publicId) return <PublicReceipt publicId={publicId} session={session} />;
  if (acknowledgementToken)
    return <AcknowledgementPage token={acknowledgementToken} />;

  if (path === '/moderation') {
    return session?.user?.role === 'MODERATOR' ||
      session?.user?.role === 'ADMIN' ? (
      <ModerationPage user={session.user} />
    ) : (
      <AuthPanel
        sessionFailure="Moderator access requires a moderator session."
        onAuthenticated={setSession}
      />
    );
  }

  if (!session?.authenticated || !session.user) {
    return <AuthPanel sessionFailure={failure} onAuthenticated={setSession} />;
  }

  return (
    <Dashboard
      session={session}
      onLogout={(next) => {
        setSession(next);
        navigate('/');
      }}
    />
  );
}
