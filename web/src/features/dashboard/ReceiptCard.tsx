import type { Receipt } from '../../shared/types';
import { formatDate } from '../../shared/presentation';
import { Status } from '../../shared/ui';

const tones: Record<Receipt['lifecycle'], string> = {
  DRAFT: 'neutral',
  ISSUED: 'success',
  REVOKED: 'danger',
};

export function ReceiptCard({
  receipt,
  onOpen,
}: {
  receipt: Receipt;
  onOpen: () => void;
}) {
  return (
    <button
      className="receipt-card"
      onClick={onOpen}
      aria-label={`Open ${receipt.title}`}
    >
      <span className="receipt-card-top">
        <Status tone={tones[receipt.lifecycle]}>{receipt.lifecycle}</Status>
        <small>{receipt.category}</small>
      </span>
      <span className="receipt-card-body">
        <span className="recipient">For {receipt.recipientLabel}</span>
        <strong>{receipt.title}</strong>
        <span>{receipt.message}</span>
      </span>
      <span className="receipt-card-foot">
        Updated {formatDate(receipt.updatedAt)} <span>View →</span>
      </span>
    </button>
  );
}
