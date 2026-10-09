import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { Receipt } from '../../shared/types';
import { ReceiptCard } from './ReceiptCard';

const receipt: Receipt = {
  id: 'receipt-1',
  recipientLabel: 'Rani',
  title: 'Held the launch together',
  message:
    'You kept the release clear and calm when the final details changed.',
  category: 'TEAMWORK',
  lifecycle: 'ISSUED',
  moderationState: 'CLEAR',
  publicId: 'public-1',
  issuedAt: '2026-10-09T10:00:00Z',
  acknowledgedAt: null,
  revokedAt: null,
  revocationReason: null,
  createdAt: '2026-10-09T09:00:00Z',
  updatedAt: '2026-10-09T10:00:00Z',
};

describe('ReceiptCard', () => {
  it('presents the real state and opens the receipt', () => {
    const onOpen = vi.fn();
    render(<ReceiptCard receipt={receipt} onOpen={onOpen} />);
    expect(screen.getByText('ISSUED')).toBeInTheDocument();
    expect(screen.getByText('Held the launch together')).toBeInTheDocument();
    fireEvent.click(
      screen.getByRole('button', { name: /open held the launch/i }),
    );
    expect(onOpen).toHaveBeenCalledOnce();
  });
});
