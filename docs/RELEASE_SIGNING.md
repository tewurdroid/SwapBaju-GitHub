# Release Signing — v2.9

The project supports signed Android release builds without storing the private keystore in Git.

Configure these GitHub Actions repository secrets:

- `ANDROID_KEYSTORE_BASE64` — base64-encoded `.jks`/`.keystore` file
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The workflow decodes the keystore only inside the temporary GitHub Actions runner and removes it when the runner is discarded.

Behavior:
- Pull requests / normal pushes: debug APK is built.
- Manual run or push to `main`: if all signing secrets exist, a signed release APK is also built.
- If secrets are absent, the workflow does not fail; it skips signed release and still uploads debug APK.

Never commit:
- `.jks`
- `.keystore`
- passwords
- private signing keys

For Play Store distribution, keep the upload/signing strategy and key management under your own secure account.
