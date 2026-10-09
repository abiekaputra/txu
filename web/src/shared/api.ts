import type {
  Acknowledgement,
  DraftInput,
  IssueResult,
  Receipt,
  Report,
  Session,
  Verification,
} from './types';

let csrfToken = '';

interface ErrorEnvelope {
  code?: string;
  message?: string;
  fields?: Record<string, string>;
}

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly fields: Record<string, string>;

  constructor(
    status: number,
    code: string,
    message: string,
    fields: Record<string, string> = {},
  ) {
    super(message);
    this.status = status;
    this.code = code;
    this.fields = fields;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body) headers.set('Content-Type', 'application/json');
  if (init.method && init.method !== 'GET')
    headers.set('X-XSRF-TOKEN', csrfToken);
  const response = await fetch(path, {
    ...init,
    headers,
    credentials: 'include',
  });
  if (!response.ok) {
    const error = (await response.json().catch(() => ({}))) as ErrorEnvelope;
    throw new ApiError(
      response.status,
      error.code ?? 'REQUEST_FAILED',
      error.message ?? 'TXU could not complete that request.',
      error.fields,
    );
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function loadSession() {
  const session = await request<Session>('/api/session');
  csrfToken = session.csrfToken;
  return session;
}

async function authenticate(path: string, body: object) {
  await request(path, { method: 'POST', body: JSON.stringify(body) });
  return loadSession();
}

export const api = {
  register: (body: { displayName: string; email: string; password: string }) =>
    authenticate('/api/auth/register', body),
  login: (body: { email: string; password: string }) =>
    authenticate('/api/auth/login', body),
  logout: async () => {
    await request('/api/auth/logout', { method: 'POST' });
    return loadSession();
  },
  listReceipts: () => request<Receipt[]>('/api/receipts'),
  receipt: (id: string) => request<Receipt>(`/api/receipts/${id}`),
  createReceipt: (body: DraftInput) =>
    request<Receipt>('/api/receipts', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  updateReceipt: (id: string, body: DraftInput) =>
    request<Receipt>(`/api/receipts/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    }),
  deleteReceipt: (id: string) =>
    request<void>(`/api/receipts/${id}`, { method: 'DELETE' }),
  issueReceipt: (id: string) =>
    request<IssueResult>(`/api/receipts/${id}/issue`, { method: 'POST' }),
  revokeReceipt: (id: string, reason: string) =>
    request<Receipt>(`/api/receipts/${id}/revoke`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    }),
  verify: (publicId: string) =>
    request<Verification>(`/api/public/receipts/${publicId}`),
  report: (publicId: string, reason: string, details: string) =>
    request<Report>(`/api/public/receipts/${publicId}/reports`, {
      method: 'POST',
      body: JSON.stringify({ reason, details }),
    }),
  acknowledgement: (token: string) =>
    request<Acknowledgement>(`/api/acknowledgements/${token}`),
  acknowledge: (token: string) =>
    request<Acknowledgement>(`/api/acknowledgements/${token}`, {
      method: 'POST',
    }),
  reports: () => request<Report[]>('/api/moderation/reports'),
  decide: (id: string, decision: string, reason: string) =>
    request<Report>(`/api/moderation/reports/${id}/decision`, {
      method: 'POST',
      body: JSON.stringify({ decision, reason }),
    }),
};
