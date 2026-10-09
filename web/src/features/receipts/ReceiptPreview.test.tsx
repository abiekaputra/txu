import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ReceiptPreview } from './ReceiptPreview';

describe('ReceiptPreview', () => {
  it('shows every field that will become immutable', () => {
    render(
      <ReceiptPreview
        draft={{
          recipientLabel: 'Dimas',
          title: 'Made complexity feel simple',
          message:
            'Your patient review made the handoff safer for everyone involved.',
          category: 'MENTORSHIP',
        }}
      />,
    );
    expect(screen.getByText('Dimas')).toBeInTheDocument();
    expect(screen.getByText('Made complexity feel simple')).toBeInTheDocument();
    expect(screen.getByText(/patient review/)).toBeInTheDocument();
    expect(screen.getByText('MENTORSHIP')).toBeInTheDocument();
  });
});
