export function formatDate(value: string | null) {
  if (!value) return 'Not yet';
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export async function copyText(value: string) {
  await navigator.clipboard.writeText(value);
}
