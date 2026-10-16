# Security status — Kachehri

**Do not distribute as a secure messenger.**

The present deterministic chat-ID-based encryption does not provide confidentiality against anyone who can derive or guess a chat ID. Current QR/safety number screens are demonstrations, not identity verification. Calls are UI prototypes, not encrypted media transport.

The Firebase rule set is deny-all by design until chat membership, real authenticated sender IDs, account isolation and adversarial tests exist. Do not open database rules simply to make the app work.

The local Room message model stores plaintext `text`; Firestore serialization also includes plaintext. A full redesign is required to remove plaintext from cloud and secure on-device storage before any real users are onboarded.

If a vulnerability is discovered, do not post user data or credentials in public issues. Do not commit API private keys, service account JSON, keystores or passwords.
