# Feature: Login（`:feature:login`）

アプリ用パスワードの**登録**・**解除（アンロック）**・**生体ログイン**を扱う機能です。

## Gradle

- **`:feature:login:api`** — ルート定義など、他 feature / app から参照する最小 API。
- **`:feature:login:impl`** — 画面・ViewModel・ユースケース・Koin。

物理パス: `feature/login/api/` と `feature/login/impl/`。

## レイヤー構成（impl）

| パッケージ | 役割 |
|------------|------|
| `presentation.screen` | `LoginScreen`, `RegisterScreen` |
| `presentation.viewmodel` | `LoginViewModel`, `RegisterViewModel` |
| `presentation.contract` | `LoginState` / `Intent` / `Effect` など |
| `presentation.operation` | `BiometricPrompt` 等 **Android 依存の手続き** |
| `presentation.biometric` | ログイン文脈の生体ハンドラ |
| `domain.usecase` | パスワード送信・生体有効状態の観測など |

## データフロー（概要）

1. ユーザー入力は **Intent** で ViewModel に集約。
2. ドメイン処理は **UseCase**、プロンプト表示は **Operation**。
3. ナビゲーションや Toast は **UiState の pending フィールド** → 画面側で処理後に consume。

## 依存の注意

- 生体認証は **`BiometricAuthenticator` を `factory { (activity) -> … }`** で取得し、ViewModel には Activity を閉じ込めない。

## 関連 README

- [api/README.md](api/README.md)
- 上位 [feature/README.md](../README.md)
