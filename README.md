# Kachehri — Sindhi Messenger (Android prototype)

**Developer:** Gahejo Dev (Zeeroz)

Native Android app built with Kotlin, Jetpack Compose, Room and optional Firebase integrations. Includes a Sindhi-language UI, chats, status, call-screen mockups and local account functionality.

## Important security and readiness notice

**Prototype only. Do not use for private or sensitive communications.** The current `E2EEEngine` derives encryption keys deterministically from a chat identifier. Anyone who knows or guesses that identifier can reproduce the key. It is **not genuine end-to-end encryption**, **not Signal/Double Ratchet**, and its safety-number/QR-like visuals are not identity verification. Encryption/decryption now fail closed instead of returning plaintext on errors, but this does **not** fix the insecure key derivation.

Local sign-up uses on-device SharedPreferences and is **not** cloud identity authentication. Accounts are local to each installation; Firebase Auth and Firestore need configuration, authorization rules and end-to-end integration tests before multi-user use. Google Sign-In also needs a valid OAuth client ID. No test account is pre-created.

## Before any production release

- Replace demo cryptography with a reviewed, authenticated E2EE protocol and secure key storage, device identity verification, and key rotation.
- Replace local-only authentication with Firebase Auth (or another verified identity provider), enforce strict Firestore rules for chat membership, and test with two real devices.
- Review disappearing messages, media and call features for actual behavior; UI alone does not imply encrypted calling.
- Add Firebase configuration privately, build with Android Studio, run tests and verify release signing and Google Play compliance.
- Audit application privacy, retention, permissions, logs and backups. Android backup is disabled for now.

## Project details

- Android package/application ID: `com.aistudio.kachehri.secmsg` (**legacy identifier**, preserved to avoid breaking installed-app identity; rename only with an intentional migration/new app ID).
- Kotlin sources currently use `com.example` package. This is a separate refactor from changing the app's visible name.
- Visible app name: **Kachehri**.

## Build

Open this repository in Android Studio and sync Gradle. Configure Firebase separately if cloud features are needed. No verified successful release build or physical-device test is claimed here.

© Gahejo Dev / Zeeroz.