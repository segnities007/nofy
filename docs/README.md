# ドキュメント索引

プロジェクトの設計・運用に関する文章の入口です。

| 文書 | 内容 |
|------|------|
| [tech.md](tech.md) | 採用技術、アーキテクチャ方針、セキュリティ・データフロー（日本語・詳細） |
| [release-signing-play.md](release-signing-play.md) | Play App Signing、アップロード鍵、CI Secrets、鍵ローテーション手順 |
| [source-layout.md](source-layout.md) | ファイル分割・パッケージ配置の実務ルール（Kotlin / Android 公式との対応） |
| [readability.md](readability.md) | 早期リターン・ガード節・宣言的 UI / UDF の指針と参照リンク |
| [compose-screen-micro-template.md](compose-screen-micro-template.md) | Compose 画面のミクロ関心事（抽象）と対応テンプレート（手順）— ルート `AGENTS.md` §4.3 から参照 |
| [codebase-refactoring-audit.md](codebase-refactoring-audit.md) | リポジトリ全体のリファクタ候補（行数・モジュール別・優先度メモ） |
| [kt-full-review.md](kt-full-review.md) | **全 `.kt` 精査**（インベントリ・行数表・監査手順・今回の修正） |
| [kt-file-inventory.txt](kt-file-inventory.txt) | `build/` 除外の全 Kotlin パス（168 件） |
| [kt-full-review-table.txt](kt-full-review-table.txt) | 各ファイルの行数と先頭 1 行（機械生成） |
| [AGENTS.md](AGENTS.md) | UI（Material 禁止範囲）、Preview 方針など開発ガイドライン |
| ルート [AGENTS.md](../AGENTS.md) | AI / コントリビュータ向けの包括的規約（Compose、セキュリティ、モジュール） |
| ルート [README.md](../README.md) | リポジトリ全体の概要（英語・OSS スタイル） |
| [../platform/README.md](../platform/README.md) | `:platform:*` と `platform/` の対応 |
| [../shared/README.md](../shared/README.md) | `:shared:*` と `shared/` の対応 |
| [../feature/README.md](../feature/README.md) | Feature モジュール構成 |

新しい長文の設計メモを追加する場合は、本ディレクトリに置き、ここからリンクを張ると見つけやすくなります。
