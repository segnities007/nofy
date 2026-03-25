# Nofy アーキテクチャ俯瞰（Mermaid）

このページの図は **手動メンテ**です。モジュール追加・ナビ変更・Koin の並び替えなどをしたら、必要に応じてエージェントや人間が Mermaid を更新してください。詳細な約束事は `AGENTS.md` と `docs/tech.md` を参照。

---

## Gradle モジュール（論理グループ）

```mermaid
flowchart TB
  subgraph app["Application"]
    A["app"]
  end
  subgraph feature["feature:*"]
    F1["login:api / impl"]
    F2["note:api / impl"]
    F3["setting:api / impl"]
  end
  subgraph platform["platform:*"]
    P1["navigation"]
    P2["localtransfer"]
    P3["database"]
    P4["storage"]
    P5["crypto"]
    P6["biometric"]
    P7["designsystem"]
  end
  subgraph shared["shared:*"]
    S1["auth"]
    S2["settings"]
  end
```

---

## 起動時 Koin（`NofyModules` の並び）

`module { … }` は危険環境検知と `SensitiveOperationGuard` の app 専用定義。

```mermaid
flowchart LR
  k0["biometricModule"] --> k1["cryptoModule"]
  k1 --> k2["datastoreModule"]
  k2 --> k3["authModule"]
  k3 --> k4["loginFeatureModule"]
  k4 --> k5["noteFeatureModule"]
  k5 --> k6["settingFeatureModule"]
  k6 --> k7["module: risk snapshot + guard"]
```

---

## 公開 NavKey（feature api の `data object`）

```mermaid
flowchart TB
  subgraph LoginRoute["LoginRoute"]
    L1["Login"]
    L2["SignUp"]
  end
  subgraph NoteRoute["NoteRoute"]
    N1["NoteList"]
  end
  subgraph SettingsRoute["SettingsRoute"]
    S1["Settings"]
    S2["VaultTransferSend"]
    S3["VaultTransferReceive"]
  end
```

---

## 認証ゲート（初回ルート・強制置換の考え方）

`NofyNavHost` の `authGateRouteOrNull` と `resolveInitialRoute` に対応。

```mermaid
flowchart TD
  S([Auth snapshot]) --> R{Registered?}
  R -->|No| U["LoginRoute.SignUp"]
  R -->|Yes| L{Locked?}
  L -->|Yes| K["LoginRoute.Login"]
  L -->|No| N["NoteRoute.NoteList"]
```

---

## ランタイムのざっくり束ね（単一 Activity）

```mermaid
flowchart TD
  App["NofyApplication\nstartKoin(nofyModules)"] --> Main["MainActivity\nFLAG_SECURE / auto-lock"]
  Main --> Nav["NofyNavHost\nNavigation3 + installers"]
  Nav --> Feat["各 feature の NavKey と画面"]
```
