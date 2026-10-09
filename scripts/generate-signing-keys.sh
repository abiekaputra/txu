#!/usr/bin/env sh
set -eu

secret_directory="${1:-.secrets}"
mkdir -p "$secret_directory"

if [ ! -f "$secret_directory/private-key.der" ]; then
  openssl genpkey -algorithm ED25519 -outform DER -out "$secret_directory/private-key.der"
  openssl pkey -inform DER -in "$secret_directory/private-key.der" -pubout -outform DER -out "$secret_directory/public-key.der"
  chmod 600 "$secret_directory/private-key.der"
  chmod 644 "$secret_directory/public-key.der"
  echo "Created local Ed25519 signing keys in $secret_directory"
else
  echo "Existing local signing keys preserved."
fi

