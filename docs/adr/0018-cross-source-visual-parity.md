# ADR-0018 — Destination redraw: Cross-Source Visual Parity

**Status:** accepted (recommended defaults applied 2026-09-17)  
**Supersedes (for product acceptance):** ADR-0005’s “Per-Platform Golden ≠ Flutter pixel match” and Program Phase-1 self-lock PASS as *visual* completion.

## Context

Phase-1 delivered Mock vertical slices and self-baselined `goldens/{android,ios,ohos}/baseline/01–26`. That gate proves **regressions against ourselves**, not **recreation of Migration Source**. Reviewers comparing Kuikly Home/Mine/Chat to Flutter see shell/simplified layouts — far from 1:1.

Flutter source: ~52 business routes, Home alone has 3 top tabs + 10+ dashboard sections; Mine is a dense dual-state dashboard. Kuikly today has thin list→detail stubs for most domains.

## Decision

### Destination (Phase-2)

Kuikly product UI must **visually match Flutter Migration Source page screenshots** for the same route + state. Phase-1 structural PASS remains historical evidence only; **visual completion requires Cross-Source Parity**.

### Gate model (recommended)

| Layer | Role |
|-------|------|
| **Flutter Reference Golden** | Source of truth. Capture Flutter on a locked Android profile (`Pixel_7_Pro` / `emulator-5554`). Stored under `goldens/flutter-ref/baseline/`. |
| **Kuikly Android vs Flutter ref** | **Hard gate.** ImageMagick AE (or MAE) with **masked status bar / home indicator**; threshold tuned per page class (chrome vs dense list). Fail = slice not done. |
| **Kuikly iOS / Ohos** | **Regression self-lock** (existing Per-Platform Golden) + Orchestrator checklist that **structure/labels/section order** match Flutter (not cross-engine pixels). |
| **Business** | Mock Backend still acceptance backend; behaviour must match Flutter Mock paths for the same screens. |

### What “一致” means

- Same **page chrome**: title, back, primary actions, tab labels, accent `#007AFF` / system greys `#F2F2F7` / white cards.
- Same **section order and copy** (Chinese labels from source).
- Same **information density** for in-scope sections (no “Shell-level” placeholders on accepted pages).
- Status bar clock / battery / network **masked** in compare.
- **Not** required: byte-identical PNGs across Flutter↔Kuikly engines; exact font metrics; real SDK players/IAP/push.

### Delivery shape

- Keep **vertical slices**, but redefine each slice as **Flutter page-cluster → Kuikly Compose parity**, not “new thin domain stub”.
- **Wave 0 (infra):** Flutter reference capture scripts + page inventory + side-by-side diff tool + mask.
- **Wave 1 (loudest gap):** Home dashboard (top Tab「首页」) + Mine root — full layout parity.
- **Wave 2+:** Re-thicken existing thin pages (Chat/Community/Search/UsedCar/…) then remaining Flutter routes by inventory priority.
- Orchestrator / WorkBuddy roles unchanged; goldens ownership stays Orchestrator.

### Out of scope (unchanged unless Destination redrawn again)

- H5, WeChat mini program, live `my_go_study` HTTP as gate.
- bfui demo routes (15 template pages).
- Pixel-perfect Flutter↔iOS↔Ohos triangle.

## Density (D1, 2026-09-17)

Flutter uses `flutter_screenutil` with `designSize 375×812`. Kuikly has no built-in ScreenUtil.

**Decision:** thin `DesignScale` / `Number.su` / `Number.susp` in `core-pager` (`pageViewWidth / 375`). Product UI that must match Flutter refs wraps with `ProvideDesignScale(pageViewWidth)`. Status bar / safeArea insets stay unscaled.

## Consequences

- `CONTEXT.md` glossary: add **Cross-Source Parity** / **Flutter Reference Golden**; amend Per-Platform Golden.
- New Program map Phase-2; Phase-1 acceptance docs keep historical PASS but are **not** visual Destination.
- Expect large UI rewrites of `MainPage` Home/Mine and many list pages; Mock models may need richer seeds to match Flutter mock copy.
