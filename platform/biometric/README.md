# `:platform:biometric`（`platform/biometric/`）

AndroidX **`BiometricPrompt`** のラッパと Koin 定義を提供します。

## 責務

- `BiometricAuthenticator` — **CryptoObject 必須**の認証（`BIOMETRIC_STRONG`）。
- `biometricModule` — `factory { (FragmentActivity) -> BiometricAuthenticator }` で **Activity をシングルトンに入れない**。

## 関連

- [`../crypto/README.md`](../crypto/README.md) の `BiometricCipher`
- feature:login / feature:setting の presentation 層
