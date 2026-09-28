# P3-E9f Retire — `14-post-detail` PASS-out (Kuikly-only route)

**Date:** 2026-09-18
**Verdict:** **RETIRED / PASS-out** — `14-post-detail` removed from the Phase-3 deferred-blocking list; no MAP unlock; Kuikly-only route, no Flutter counter-source page.

This file closes the `14-post-detail` deferred row that has been carried since E1-GATE ([104](104-p3-e1-gate-map12.md), [117](117-p3-e3-deferred-ceilings.md), [159](159-p3-e8-deferred-ceilings.md)). Per the **canonical KDoc on `PostDetailPage.kt:55-58`** and the **authoritative Flutter route table** (`/Users/mac/Desktop/github/my_ai_project/commons/wys_router/lib/src/route/route_path.dart`), the Flutter community surface has **no** post-detail route — post media lives inside the feed's `PostCardWidget` + `ImageGridWidget` (`features/community/lib/community/widgets/post_card_widget.dart:15`, `image_grid_widget.dart:6`); the only community routes declared in Flutter are `community` (`/community`) and `communityPublish` (`/community/publish`), neither of which is a post-detail page.

## Why retire (not re-source)

- **Flutter has no `PostDetailPage`.** `route_path.dart:25-26` declares only `community` + `communityPublish`; no `post_detail`/`postDetail` constant exists. Grep of `features/community/lib/community/**/*.dart` finds `PostCardWidget` (a feed card) and `ImageGridWidget` (media inside the card) — no separate route.
- **Kuikly `PostDetail` is Kuikly-only.** `feature-home/src/commonMain/kotlin/com/example/kuikly/feature/home/pages/PostDetailPage.kt:52-66` KDoc explicitly states "Flutter Community 模块**没有**帖子详情页（帖子卡即详情，走评论 sheet）" and routes the "Flutter source" to the feed card itself (single-segment `Post.content` rich text + `@user` / `#topic` color tokens). The route exists in Kuikly to render a detail view of one feed post; Flutter chose an in-card layout instead.
- **Prior Flutter-ref `14` was never a true post-detail frame.** The size-gate `14-flutter-post-detail.png` was rejected by E9a ([165](165-p3-e9a-acceptance.md)) as **Home mis-lock** — same class as `08-flutter-settings` (E3a / E4) and `17-flutter-video-list` (E7f / E8b). No UI-tap fallback exists (unlike settings/video-list/pay-list/music-list) because Flutter has no UI surface to land on after tap.
- **`26-pay-confirm` precedent.** E9e ([164](164-p3-e9-retire-payconfirm.md)) retired a similar Kuikly-only mock pair for the same structural reason: "no Flutter page exists to recapture, so the baseline would always be UNRELIABLE." Holding the deferred row buys nothing.
- **Photo-density ceiling is moot.** The E2a assets-and-blocks attempt ([103](103-p3-e2a-acceptance.md)) raised Kuikly's media coverage but RMSE_rel still sat ≈0.34 after E9a. With no Flutter target to diff against, the density ceiling register row drops with this retire (it can never be cleared).

## Status registered: PASS-out (Kuikly-only route)

| Page | Status | Rationale |
|------|--------|-----------|
| `14-post-detail` | **PASS-out** | Kuikly-only route; Flutter surface is the in-card `PostCardWidget` on the community tab, not a separate page. Carries the `14-post-detail` stem (`scripts/golden-{android,ios,ohos}-capture.sh`) but is **not** a Cross-Source pair. |

`14-post-detail` is **not** added to `scripts/golden-vs-flutter-diff.sh` `MAP`. The existing commented placeholder at `scripts/golden-vs-flutter-diff.sh:39` (`# pending photo-density (RMSE≈0.34 after E2a): MAP[${#MAP[@]}]="14-post-detail=14-flutter-post-detail"`) is the historical "would-be pair"; it stays commented and the deferred-blocking row drops from the runbook.

## Deferred remaining (Phase-3 frozen)

After E9f retire, the deferred-blocking list drops from **3 → 2**:

| Pair | RMSE_rel | Deferral reason |
|------|----------|------------------|
| `19-friend-list=19-flutter-friend-list` | — | Flutter `FriendPage` is a **stub** — no real list to gate against |
| `23-live-list=23-flutter-live-list` | — | Flutter-ref **UNRELIABLE** ([148](148-p3-e7-gate-map17.md)) |

`14-post-detail` is no longer deferred. MAP 18/18 unchanged ([158](158-p3-e8-gate-map18.md)); `26-pay-confirm` retired earlier by E9e ([164](164-p3-e9-retire-payconfirm.md)).

## File-lock changes

- Created: `docs/evidence/my-ai-migration/parity/169-p3-e9-retire-postdetail.md` (this file).
- Updated: `.scratch/my-ai-migration/parity/INVENTORY.md` — deferred list drops `14-post-detail`, count 3 → 2; deferred register row removed.
- Updated: `docs/runbooks/visual-parity.md` deferred table — removed `14-post-detail` row; added retire note alongside the existing `26-pay-confirm` retire note (deferred note only, per handoff file-lock).
- Untouched: `scripts/golden-vs-flutter-diff.sh` — the commented `14-post-detail=14-flutter-post-detail` placeholder stays as historical context.
- Untouched: `scripts/golden-{android,ios,ohos}-capture.sh` — `14-post-detail` stem still captured for self-lock on each platform; Kuikly-only route continues to exist.
- Untouched: `feature-home/.../PostDetailPage.kt` — Kuikly-only page continues to render the feed card style (V2c / V4d polish survives).
- Untouched: golden baselines, route inventory rows, product code outside the file-lock.

## Executor Report
### Status
- [x] Done — evidence file written at `docs/evidence/my-ai-migration/parity/169-p3-e9-retire-postdetail.md`
- [x] INVENTORY.md deferred row + refresh note updated
- [x] `docs/runbooks/visual-parity.md` deferred row removed + retire note added

### Evidence
- Cited [158](158-p3-e8-gate-map18.md) — MAP 18/18 PASS, unchanged by E9f retire
- Cited [159](159-p3-e8-deferred-ceilings.md) — frozen Phase-3 deferred register that E9f amends (drops `14-post-detail` row)
- Cited [104](104-p3-e1-gate-map12.md) — first Phase-3 hard gate that froze the `14-post-detail` row at RMSE 0.345
- Cited [165](165-p3-e9a-acceptance.md) — Flutter-ref `14` accepted as Home mis-lock; density alone insufficient
- Cited [103](103-p3-e2a-acceptance.md) — Kuikly `PostDetail` media block land (assets `app-shared/.../assets/common/post_detail/media_{1,2}.png`); ceiling persisted
- Cited [117](117-p3-e3-deferred-ceilings.md) — first canonical registration of the `14-post-detail` photo-density ceiling
- Cited [164](164-p3-e9-retire-payconfirm.md) — `26-pay-confirm` PASS-out precedent for Kuikly-only routes with no Flutter counterpart
- Cited `feature-home/src/commonMain/kotlin/com/example/kuikly/feature/home/pages/PostDetailPage.kt:52-66` — KDoc statement that Flutter has no post-detail page; route exists in Kuikly only
- Cited `/Users/mac/Desktop/github/my_ai_project/commons/wys_router/lib/src/route/route_path.dart:25-26` — only `community` + `communityPublish` declared; no `postDetail` constant
- Cited `features/community/lib/community/widgets/post_card_widget.dart:15` and `image_grid_widget.dart:6` — Flutter surface is in-card media (feed card, not a route)
- File-lock honored: evidence + INVENTORY + runbook deferred row only; no product code, no scripts (MAP stays 18), no golden edits, no diff-script unlock

DONE_P3_E9F
