import type { PropsWithChildren, ReactNode } from 'react';

export function Brand() {
  return (
    <a className="brand" href="/" aria-label="TXU home">
      <img src="/txu-mark.png" alt="" />
      <span>TXU</span>
    </a>
  );
}

export function Shell({
  children,
  actions,
}: PropsWithChildren<{ actions?: ReactNode }>) {
  return (
    <>
      <header className="site-header">
        <Brand />
        <nav className="header-actions">{actions}</nav>
      </header>
      <main>{children}</main>
      <footer>TXU · verifiable appreciation, thoughtfully issued.</footer>
    </>
  );
}

export function Status({
  children,
  tone = 'neutral',
}: PropsWithChildren<{ tone?: string }>) {
  return <span className={`status status--${tone}`}>{children}</span>;
}

export function Notice({
  children,
  tone = 'info',
}: PropsWithChildren<{ tone?: string }>) {
  return (
    <div
      className={`notice notice--${tone}`}
      role={tone === 'danger' ? 'alert' : 'status'}
    >
      {children}
    </div>
  );
}

export function Spinner({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="loading" role="status">
      <span className="spinner" /> {label}
    </div>
  );
}
