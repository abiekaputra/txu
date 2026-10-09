# Security Policy

TXU is a local portfolio product. It demonstrates secure product boundaries but
does not provide legal identity verification or a public hosted service.

## Supported version

The current `main` branch is supported.

## Report a vulnerability

Open a private GitHub security advisory for the repository. Do not include
passwords, session cookies, private signing keys, or acknowledgement links in a
public issue.

## Operational guidance

- Replace every value marked local or demo before using a shared environment.
- Keep `.secrets/private-key.der` outside version control and back it up if
  historical receipts must remain verifiable.
- Disable the `demo` Spring profile outside local demonstration.
- Terminate TLS in front of the web container and set `COOKIE_SECURE=true`.
- Rotate database credentials and the report fingerprint salt.

The threat model and controls are documented in
[`docs/architecture/security-model.md`](docs/architecture/security-model.md).
