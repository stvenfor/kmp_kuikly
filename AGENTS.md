# AGENTS.md — kmp_kuikly

This project uses **[Agency Agents](https://github.com/msitarzewski/agency-agents)** for specialized develop / test / accept roles, grounded by Kuikly project docs.

## Installed

| Scope | Location | Divisions |
|---|---|---|
| This repo (Cursor) | `.cursor/rules/*.mdc` | `engineering` + `testing` (68 agents) |
| This machine (Claude Code) | `~/.claude/agents/` | same subset |
| Always-on binder | `.cursor/rules/kuikly-project-context.mdc` | forces Kuikly vocabulary |
| Always-on minimalism | `.cursor/rules/ponytail.mdc` | [Ponytail](https://github.com/DietrichGebert/ponytail) YAGNI ladder (+ Kuikly overrides) |

Source checkout used for install: `/Users/mac/agency-agents`.

## How to activate in Cursor

Agency rules are `alwaysApply: false`. Activate by naming the role in chat, for example:

- 「用 **Mobile App Builder** 实现 ticket 03」
- 「用 **Evidence Collector** 验收 Android 竖切，要截图证据」
- 「用 **Reality Checker** 做最终门禁，默认 NEEDS WORK」
- 「用 **Code Reviewer** 审本次 diff」
- 「用 **DevOps Automator** 加 PR CI」

The binder rule (`kuikly-project-context`) and **Ponytail** are always on.

### Ponytail vs architecture

Ponytail governs *how much code* inside a ticket. It does **not** override ADRs / SCHEME / open tickets (e.g. heavy Feature + Platform Extension layout, four-platform ports). See ADR-0004.

Intensity (Claude Code / Codex plugins): `lite` | `full` (default) | `ultra` | `off` via `PONYTAIL_DEFAULT_MODE` or `~/.config/ponytail/config.json`. Cursor is instruction-only — no slash modes; say「stop ponytail」/「ponytail ultra」in chat if needed.

## Suggested role map (Demo Skeleton)

| Phase | Role | Job |
|---|---|---|
| Develop | Mobile App Builder | Kuikly Feature / Shell work per ticket |
| Develop | DevOps Automator | CI, version catalog, runbooks |
| Review | Code Reviewer | Diff vs ADR + SCHEME |
| Test | Test Results Analyzer / Test Automation Engineer | common tests; H5 only if ticket says so |
| Accept | Evidence Collector | screenshots + command output |
| Accept | Reality Checker | evidence gate; no fantasy PASS |

## Project truth sources

1. `CONTEXT.md`
2. `docs/SCHEME.md`
3. `docs/adr/*`
4. Tickets under `.scratch/demo-skeleton/issues/` (when published)

## Git commit policy

- 提交说明**必须使用中文**（至少含汉字）
- **禁止** `Co-authored-by: Cursor <cursoragent@cursor.com>`

Enforced by:

- Cursor hooks: `.cursor/hooks.json`（`sessionStart` + `beforeShellExecution`）
- Git hook: `.githooks/commit-msg`（安装：`./scripts/install-git-hooks.sh`）

Installed from [Tencent-TDS/KuiklyUI-AI](https://github.com/Tencent-TDS/KuiklyUI-AI) into `.agents/skills/` (lockfile: `skills-lock.json`).

```bash
# reinstall / update (prefer SSH if HTTPS clone hangs)
git clone --depth 1 git@github.com:Tencent-TDS/KuiklyUI-AI.git /tmp/KuiklyUI-AI
npx skills add /tmp/KuiklyUI-AI/skills -a cursor -s '*' -y --copy
```

Useful for this repo: `kuikly-compose-ui-framework`, `kuikly-multi-module-config`, `kuikly-expand-api`, `kuikly-expand-view`.

## Reinstall / update

From project root:

```bash
/Users/mac/agency-agents/scripts/install.sh \
  --tool cursor \
  --division engineering,testing \
  --no-interactive
```

Machine-wide Claude Code:

```bash
/Users/mac/agency-agents/scripts/install.sh \
  --tool claude-code \
  --division engineering,testing \
  --no-interactive
```

Do not overwrite `kuikly-project-context.mdc` when reinstalling (installer only adds agency `*.mdc` slugs; this binder is project-owned).
