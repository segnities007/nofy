---
name: nofy-product-improvement
description: Guides research and improvement work for the nofy Android app (offline encrypted notes vault, Jetpack Compose, Koin, SQLCipher, modular Clean Architecture). Use when the user asks to improve the product, run product/security/UX/performance investigations, prioritize changes, or evaluate ideas against nofy conventions and threat model.
---

# Nofy product improvement

## Scope

**nofy** is a local-first notes vault: SQLCipher + field encryption, Android Keystore, Argon2, BiometricPrompt, lockout, heuristic environment checks, `FLAG_SECURE`, auto-lock. UI: Jetpack Compose via `:platform:designsystem`. Architecture: feature `api`/`impl`, `platform/*`, `shared/*`, Koin, UDF (`UiState` / `Intent` / `Effect`) per `AGENTS.md`.

This skill steers **how** to improve the product without contradicting repo rules.

## Before proposing or implementing

1. Read **`AGENTS.md`** (Compose, design system, VM/UI boundaries, no drive-by refactors).
2. Skim **`docs/tech.md`** and **`docs/source-layout.md`** for stack, security posture, and file placement.
3. Prefer **minimal diffs** scoped to the improvement; do not “clean up” unrelated code.

## Research: source reliability

| Tier | Use for | Examples |
|------|---------|----------|
| A | Decisions that affect crypto, backup, or platform APIs | [Android security / cryptography / backup](https://developer.android.com/privacy-and-security/security-best-practices), [OWASP MASTG](https://mas.owasp.org/MASTG/) |
| B | Compose performance, Baseline Profiles | [Compose performance](https://developer.android.com/develop/ui/compose/performance), [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles) |
| C | Ideas only — verify against A before code | Blogs, Medium, competitor marketing |

State tier when citing. Never treat tier C as proof.

## Improvement axes (pick explicitly)

1. **Data lifecycle & trust** — Auto Backup / `dataExtractionRules` vs encrypted DB; user expectations for export, wipe, and data loss; alignment with `docs/tech.md` threat model (heuristics are not perfect tamper-proofing).
2. **Security UX** — False positives from environment detection: recovery path, copy for settings/help, avoid generic failures for enrollment invalidation (biometric).
3. **Compose performance** — Hot paths (note editor, pager): stability annotations, `remember` / `derivedStateOf`, Lazy keys, defer state reads; measure with Studio recomposition tracing before large refactors.
4. **Accessibility** — TalkBack on critical path: unlock → note edit → settings; `contentDescription`, focus order, toggles; short manual pass beats long article research.
5. **Build & delivery** — Baseline Profile + Macrobenchmark for cold start or post-unlock home; module dependency rules (feature `api` not depending on other features’ `api`/`impl`).
6. **Consistency** — Domain in `shared:auth` under `com.segnities007.auth.domain.*` (no Android in domain packages by convention); feature patterns match `docs/source-layout.md`.

## Output format for “improvement reports”

Use this structure (concise):

```markdown
## Goal
[User-visible or measurable outcome]

## Findings
- [Bullet + tier A/B/C if external]

## Recommendations
1. [Actionable, scoped] — Effort: S/M/L — Risk: low/med/high

## Repo alignment
[How this respects AGENTS.md / design system / modules]

## Out of scope / defer
[What not to do now]
```

## Checklists (quick)

**Backup / extraction**

- [ ] Manifest and backup rules match intent (exclude or encrypt sensitive paths).
- [ ] Documented for users (what is / is not backed up).

**Security-sensitive change**

- [ ] Threat model sentence updated if assumptions change.
- [ ] No new stringly-typed domain errors; typed errors + UI copy in presentation.

**UI / Compose**

- [ ] No `Theme` in feature screens at runtime (root only).
- [ ] Tokens/components from `:platform:designsystem` for chrome that repeats.

## Anti-patterns for this project

- Broad modularization or new Gradle modules without a stated benefit (build time, encapsulation, or delivery).
- Duplicating Material in features instead of design system.
- Putting `Context`, resources, or navigation inside `ViewModel` / domain use cases.
- “Hardening” that worsens UX with no documented false-positive handling.

## Optional deep dive

For link-only reference material (official URLs), see [reference.md](reference.md).
