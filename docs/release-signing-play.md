# Play 配信・署名・鍵ローテーション

Google Play へのリリースと **Play App Signing** 前提での **アップロード鍵** の扱いをまとめます。機密はリポジトリに置かず、ローカルは `keystore.properties`、CI は GitHub Secrets を使います。

## Play App Signing の役割分担

| 鍵 | 役割 | 誰が保持するか |
|----|------|----------------|
| **アプリ署名鍵**（App signing key） | ストア配布ビルドに最終署名 | **Google Play**（有効化後は開発者が直接保持しない） |
| **アップロード鍵**（Upload key） | Play にアップロードする AAB/APK に署名 | **開発者／CI**（本リポジトリではローカルファイルまたは Secrets 経由） |

初回に Play Console でアプリを作成し **Play App Signing を有効**にすると、アップロード鍵とアプリ署名鍵は分離されます。以降の端末向け署名は Google 側の鍵で行われます。

公式: [アプリ署名の仕組み](https://support.google.com/googleplay/android-developer/answer/9842756)（Use Play App Signing）

## 本プロジェクトでのビルド・配信

### ローカル release ビルド

- ルートに `keystore.properties` を置く（**git 管理外**。`.gitignore` 済み）。
- プロパティ: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`（`app/build.gradle.kts` が読み込み）。
- または環境変数 `RELEASE_KEYSTORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`。

### CI（内部トラックへのアップロード）

ワークフロー [`.github/workflows/deploy-play.yml`](../.github/workflows/deploy-play.yml) が Fastlane で `:app:bundleRelease` を実行し、AAB を内部トラックへ送ります。

必要な **GitHub Actions の Secrets**（名前はワークフローと一致）:

| Secret | 内容 |
|--------|------|
| `PLAY_STORE_JSON_KEY` | Play Console のサービスアカウント用 JSON 全文（ファイルは `.gitignore` の `play-store-credentials.json` に相当） |
| `ANDROID_KEYSTORE_BASE64` | アップロード用 keystore を base64 エンコードしたもの |
| `ANDROID_KEYSTORE_PASSWORD` | keystore のパスワード |
| `ANDROID_KEY_ALIAS` | キーエイリアス |
| `ANDROID_KEY_PASSWORD` | キーのパスワード |

サービスアカウントの権限・JSON の取得手順は [Google Play Developer API のセットアップ](https://developers.google.com/android-publisher/getting_started) を参照してください。

## アップロード鍵のローテーション（紛失・漏えい・定期更新）

アップロード鍵は **Play に届くバイナリの署名**に使うだけで、**アプリ署名鍵のローテ**とは別手続きです。

### 1. Play Console で「アップロード鍵のリセット」を依頼

1. [Play Console](https://play.google.com/console) → 対象アプリ → **設定** → **アプリの整合性**（または **App signing**）。
2. **アップロード鍵をリセット**（Reset upload key）の流れに従い、新しいアップロード用証明書を登録します。  
   公式: [アップロード鍵を紛失した場合](https://support.google.com/googleplay/android-developer/answer/9842756#reset)

新しい keystore（または同じ keystore 内の新エイリアス方針）はチームのポリシーに合わせて生成します。例:

```bash
keytool -genkeypair -v -keystore upload-new.jks -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

### 2. シークレットとローカル設定の更新

- **GitHub**: 上記 `ANDROID_KEYSTORE_*` を新 keystore に合わせて更新。`ANDROID_KEYSTORE_BASE64` は新ファイルを `base64 -w0 upload-new.jks` 等で再生成。
- **ローカル**: `keystore.properties` の `storeFile` / パスワード / エイリアスを新鍵に合わせる。

### 3. 検証

- ローカルまたは CI で `bundleRelease` が成功し、Play にアップロードできることを確認。

### アプリ署名鍵のローテーション

Google が保持する **アプリ署名鍵**の変更は、セキュリティインシデント時などに Play Console から別プロセスで行います。アップロード鍵のリセットとは独立です。手順は公式の「キーのアップグレード」／サポート案内に従ってください。

## 運用上の注意

- **keystore・JSON・パスワードをコミットしない**（現状の `.gitignore` を維持）。
- 退職・端末紛失時は、アップロード鍵へのアクセス経路（Secrets 共有、ローカルコピー）も棚卸しする。

## 依存関係スキャン（推奨アクション 1 に対応）

- **Dependabot**: [`.github/dependabot.yml`](../.github/dependabot.yml) で Gradle と GitHub Actions を週次で更新確認。
- **Dependency graph**: デフォルトブランチへの push で [`.github/workflows/dependency-submission.yml`](../.github/workflows/dependency-submission.yml) が Gradle の依存関係を GitHub に提出し、Dependabot アラートの材料にします。プライベートリポジトリでは組織設定で **Dependency graph** が有効である必要があります。
- **Dependency review**: PR 時に [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) 内のジョブが既知の脆弱性を比較します。パブリックリポジトリ、または GitHub Advanced Security 有効時にフル活用できます。
