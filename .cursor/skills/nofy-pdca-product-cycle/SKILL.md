---
name: nofy-pdca-product-cycle
description: Runs a self-contained Plan-Do-Check-Act loop to improve the nofy Android product (scope, implementation, verification, and follow-up) without open-ended scope creep. Use when the user asks for PDCA, continuous improvement cycles, iterative product betterment, or a structured loop from hypothesis to shipped change and retrospective.
---

# Nofy PDCA product cycle

## Intent

One **closed loop** per invocation: **Plan → Do → Check → Act**. The agent finishes with artifacts (what changed, how verified, what to do next). No endless refactors; align with `AGENTS.md` and minimal diffs.

## When to start

User asks for PDCA, improvement cycle, iterative polish, or “make it better in a structured way.” If the goal is vague, **Plan** must first narrow to one measurable slice (one screen, one flow, one metric, one risk).

## P — Plan (exit criteria before coding)

Produce explicitly:

1. **Objective** — One sentence, user- or quality-visible (e.g. “Reduce failed unlock confusion”, “Faster note screen first frame”).
2. **Hypothesis** — If we change X, we expect Y because Z.
3. **Scope boundary** — Files/modules likely touched; **out of scope** list (max 3 bullets).
4. **Success signals** — At least one: build/test command, observable UI behavior, or doc update that proves done.
5. **Risk** — What could break (security, navigation, lock flow); rollback = revert commit.

If Plan cannot meet (1)–(5), stop and ask one clarifying question or propose the smallest valid objective.

## D — Do

1. Read **`AGENTS.md`** and relevant **`docs/tech.md`** / **`docs/source-layout.md`** for the touched area.
2. Implement **only** what Plan scoped. Prefer design system (`:platform:designsystem`), UDF patterns, no `Theme` in feature runtime.
3. Run **`./gradlew check`** (or the success signal from Plan) before declaring Do complete.

## C — Check

1. **Automated** — Result of the command(s) from Plan; fix failures or narrow scope.
2. **Intent** — Does the change match the hypothesis? Any regression on critical path (login → note → lock)?
3. **Evidence** — Short note: what was run, pass/fail. No pass without running checks when the repo is available.

If Check fails: either fix within scope or **Act = revert + document failed hypothesis** (still a valid cycle).

## A — Act

1. **Consolidate** — If the change establishes a pattern, add or adjust **one** of: inline comment only when non-obvious, existing doc section, or rule—no new doc files unless the user asked.
2. **Next cycle seed** — One bullet: recommended next Plan objective (or “none; monitor in production/usage”).
3. **Handoff summary** — Fixed format below (copy-paste ready).

```markdown
## PDCA summary
- **Objective:**
- **Done (Do):**
- **Verified (Check):** [commands + result]
- **Act:** [standardized / deferred / reverted — what next]
```

## Anti-patterns

- Starting Do without a written Plan boundary.
- Mixing multiple hypotheses in one cycle.
- Skipping `./gradlew check` when the environment can run it.
- Drive-by refactors outside Plan scope.

## Relation to other project skills

For research source tiers and product context, combine with [.cursor/skills/nofy-product-improvement/SKILL.md](../nofy-product-improvement/SKILL.md) during **Plan** (hypothesis + evidence expectations).
