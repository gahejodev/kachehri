# Kachehri Play Store release gates

Status: **NOT READY TO PUBLISH**. This document is a release blocker checklist, not a completion claim.

- [ ] Firebase project created and Android app registered; package ID agreed and configured.
- [ ] Firebase email/Google sign-in tested on two devices; OAuth SHA fingerprints and Google services configured.
- [ ] Firebase security rules redesigned around verified participant UIDs and tested in emulator with cross-user attack cases. The supplied firestore.rules DENIES ALL by default.
- [ ] Chat IDs and message sender IDs derived from authenticated identities, not local random contacts or hard-coded 'me'.
- [ ] E2EE implemented using audited protocol with per-device identity keys, authenticated key exchange, forward secrecy, verification and recovery. Current deterministic keys are insecure.
- [ ] Real-time messaging delivery/read receipts, offline retry, account isolation and message expiry verified end to end.
- [ ] Call screens replaced with real calling implementation or removed from production UI.
- [ ] Account deletion UX and backend data deletion/retention process completed; public deletion request URL prepared.
- [ ] Public privacy policy URL and accurate Play Data safety answers published.
- [ ] App identity, icon, copyright, screenshots, content rating and store listing checked.
- [ ] Signed AAB built, installed and tested on physical devices; Android lint, unit tests, instrumentation tests pass.
- [ ] Internal/closed Play testing completed and production rollout reviewed.

Never commit release signing keys, credentials or service-account private keys.
