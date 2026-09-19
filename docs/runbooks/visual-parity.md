# Visual Parity runbook (Phase-2 / Phase-3)

**ADR:** [0018](../adr/0018-cross-source-visual-parity.md)  
**Plan:** [VISUAL-PARITY-PHASE2.md](../architecture/VISUAL-PARITY-PHASE2.md)

## Devices

| Role | Profile |
|------|---------|
| Flutter ref + Kuikly Android | `emulator-5554` (Pixel_7_Pro class) |
| Kuikly iOS / Ohos | existing runbooks; regression self-lock only |

## Mapped pairs (Cross-Source MAP)

`scripts/golden-vs-flutter-diff.sh` diffs Kuikly Android `actual/` vs `goldens/flutter-ref/baseline/` (masked RMSE_rel ≤ `GOLDEN_RMSE_MAX`, default 0.22). MAP is defined in that script:

| Kuikly actual | Flutter ref | S1 RMSE_rel |
|---|---|---|
| `02-main-home` | `02-flutter-main-home` | 0.141 |
| `06-main-me-guest` | `03-flutter-main-me-guest` | 0.146 |
| `07-main-me-logged-in` | `08-flutter-main-me-logged-in` | 0.164 |
| `03-login` | `07-flutter-login` | 0.115 |
| `10-search` | `10-flutter-search` | 0.144 |
| `04-usedcar-list` | `04-flutter-usedcar-list` | 0.073 |
| `11-main-chat` | `11-flutter-main-chat` | 0.080 |
| `13-main-community` | `13-flutter-main-community` | 0.197 |
| `12-chat-detail` | `12-flutter-chat-detail` | 0.091 |

`02b-flutter-main-home-logged-in` is captured on the Flutter side, and `02b-main-home-logged-in` is captured Kuikly-side. After E1b home photo assets landed, the Cross-Source MAP pair is now **promoted** (RMSE_rel ≈ 0.192, under the 0.22 default); the historical photo/icon ceiling rationale lives in `91` and the E1b evidence `102`.

## Capture caveats (Phase-2)

- **Android cold start ~9s.** Main/Search settle is raised (8s) but a cold-start blank frame is ~30KB and can still be captured. **Size-poll the PNG** (settled Main/Search ≈ >120KB) and recapture rather than trusting the settle sleep — under-settled blanks caused interim RMSE fails on 02/06/10 in S1.
- **Flutter `12-chat-detail` is flaky, never lock it blindly.** `chat_detail_via_tap` cold-starts → Chat tab → taps row 1, gated by `CHAT_DETAIL_MIN_BYTES` (default `100000`; `120000` rejected a valid 119629-byte UI) **plus** a bottom-input-bar check. On failure it falls back to the `chat/detail` deeplink; if that is also under the byte gate or lacks the input bar the shot is **deleted**, and `--update` will not clobber a prior good `12` baseline.
- **Flutter logged-in runs** can hit VIP/SVIP interstitials (`确认开通`) or drop the session (auth gate) mid-capture. The tap path back-navigates / re-logs, but re-check every locked stem before accepting.
- Kuikly avatars stay letter placeholders vs Flutter photos (no image SDK) — accepted ceiling; RMSE stays under max.

## Commands

```bash
# 1) Capture Flutter references
./scripts/flutter-ref-capture.sh --update

# 2) Capture Kuikly Android
./scripts/golden-android-capture.sh

# 3) Cross-source gate (mapped pairs in script)
./scripts/golden-vs-flutter-diff.sh

# 4) iOS / Ohos regression
./scripts/golden-ios-diff.sh
./scripts/golden-ohos-diff.sh
```

## Soft Gate (ADR-0018)

iOS / Ohos are **self-lock only** — no cross-source pairs. Re-lock + verify after UI waves:

```bash
# iOS / Ohos self-lock (re-lock then diff → expect all AE=00)
./scripts/golden-ios-capture.sh  --update && ./scripts/golden-ios-diff.sh
./scripts/golden-ohos-capture.sh --update && ./scripts/golden-ohos-diff.sh

# Android self-baseline after any Phase-2 UI wave, then cross-source
./scripts/golden-android-capture.sh --update && ./scripts/golden-android-diff.sh
./scripts/golden-vs-flutter-diff.sh

# Flutter refs only when mock data / UI source changed
./scripts/flutter-ref-capture.sh --update
```

## Evidence

- [77 — RMSE cross-source hard gate PASS](../evidence/my-ai-migration/parity/77-rmse-gate-pass.md) (8 pairs, pre-`12`)
- [86 — P2 Soft Gate S1 PASS](../evidence/my-ai-migration/parity/86-p2-s1-soft-gate-pass.md) (MAP 9/9, `12-chat-detail` RMSE_rel 0.091, iOS + Ohos self-lock)
- [91 — P2 Soft Gate S2 PASS](../evidence/my-ai-migration/parity/91-p2-s2-soft-gate-pass.md) (Android self-lock 26/26 AE=00, `02b` rationale captured pre-promotion — see Phase-3 below)

## Phase-3 — Cross-Source MAP expansion

**Destination status.** Phase-2 hard gate + S1/S2 Soft Gates **PASS** (`86` + `91`); Cross-Source MAP is **33/33** locked (Phase-6 Wave5 → 32/32 [177](../evidence/my-ai-migration/parity/177-p6-e2-gate-map32.md); Phase-7 `24-live-detail` stub flatten [179](../evidence/my-ai-migration/parity/179-p7-e1-live-detail-unlock.md)). Splash PASS-out. Phase-3 widens coverage beyond the initial 9 pairs — Kuikly pages that have a settled Flutter reference and stay under the RMSE gate move from "self-lock only" to "cross-source gated".

### MAP20 — current hard Cross-Source pairs

Added/promoted across the MAP14+ passes (see [125 — P3-E3 gate MAP14](../evidence/my-ai-migration/parity/125-p3-e3-gate-map14.md) onward):

| Pair | RMSE_rel | Source |
|------|----------|--------|
| `05-usedcar-detail=05-flutter-usedcar-detail` | ≈0.151 | Wave3 capture (E1a2 harden) |
| `09-login-password=09-flutter-login-password` | ≈0.148 | Wave3 capture (E1a2 harden) |
| `02b-main-home-logged-in=02b-flutter-main-home-logged-in` | ≈0.192 | promoted after E1b home photo assets ([102](../evidence/my-ai-migration/parity/102-p3-e1b-acceptance.md)); was ≈0.250 |
| `25-pay-list=25-flutter-pay-list` | **≈0.159** | promoted after E2e/E2f Flutter-ref via AllServices→会员续费 UI tap ([109](../evidence/my-ai-migration/parity/109-p3-e2e-acceptance.md) / [110](../evidence/my-ai-migration/parity/110-p3-e2f-acceptance.md)); was UNRELIABLE |
| `15-music-list=15-flutter-music-list` | **≈0.145** | promoted after E3 ([125](../evidence/my-ai-migration/parity/125-p3-e3-gate-map14.md)): Flutter-ref via AllServices→音频列表 multi-scroll (E3b2), Kuikly E3e 72dp row; was UNRELIABLE (`音频列表` label not found) |
| `08-settings=08-flutter-settings` | **≈0.103** | promoted after E4 ([140](../evidence/my-ai-migration/parity/140-p3-e4-gate-map15.md)): prior Flutter baseline was a **Home** frame mis-lock (459KB Home vs Settings), reinstall + privacy dismiss + guest Mine UI-tap to「设置」yielded real Settings ref (230426B); combined with E3a/E4e2 flatten RMSE_rel dropped from ≈0.237 to 0.103 |
| `18-video-detail=18-flutter-video-detail` | **≈0.211** | promoted after E7 Wave4 ([148](../evidence/my-ai-migration/parity/148-p3-e7-gate-map17.md)): Flutter-ref Wave4 locked + Kuikly actuals recaptured 2026-09-18 |
| `21-classroom-list=21-flutter-classroom-list` | **≈0.141** | promoted after E7 Wave4 ([148](../evidence/my-ai-migration/parity/148-p3-e7-gate-map17.md)): Flutter-ref Wave4 locked + Kuikly actuals recaptured 2026-09-18 |
| `17-video-list=17-flutter-video-list` | **≈0.154** | promoted after E8 ([156](../evidence/my-ai-migration/parity/156-p3-e8b-acceptance.md) / [158](../evidence/my-ai-migration/parity/158-p3-e8-gate-map18.md)): E8a content gate + E8b true Flutter-ref (prior baseline was a Home mis-lock, ≈0.259 — [149](../evidence/my-ai-migration/parity/149-p3-e7f-acceptance.md)); MAP 18/18 gate 2026-09-18 |
| `19-friend-list=19-flutter-friend-list` | **≈0.032** | promoted after E9g ([172](../evidence/my-ai-migration/parity/172-p3-e9g-friend-live-unlock.md)): Kuikly `FriendListPage` flattened to Flutter `Friend 模块` stub; Flutter-ref true stub (55KB), not Home |
| `23-live-list=23-flutter-live-list` | **≈0.032** | promoted after E9g ([172](../evidence/my-ai-migration/parity/172-p3-e9g-friend-live-unlock.md)): Kuikly `LiveListPage` flattened to Flutter `进入 Mock 直播房` stub; Flutter-ref true stub (57KB) |

**Deferred:** none. `14-post-detail` and `26-pay-confirm` are PASS-out (no Flutter surface), not deferred.

`26-pay-confirm` retired (PASS-out) per [164](../evidence/my-ai-migration/parity/164-p3-e9-retire-payconfirm.md) — Kuikly-only mock (`PayConfirmPage.kt:38-47`); Flutter `features/pay/lib/pay/view/pay_page.dart` is a placeholder stub with no confirm-step surface, so no Cross-Source pair exists.

`14-post-detail` retired (PASS-out) per [169](../evidence/my-ai-migration/parity/169-p3-e9-retire-postdetail.md) — Kuikly-only route (`PostDetailPage.kt:52-66` KDoc explicitly states Flutter Community has no post-detail page; the Flutter surface is the in-card `PostCardWidget` + `ImageGridWidget` on the community tab, not a separate route); Flutter-ref `14` was a Home mis-lock ([165](../evidence/my-ai-migration/parity/165-p3-e9a-acceptance.md)), so no Cross-Source pair can be locked. Predecessor evidence: [103](../evidence/my-ai-migration/parity/103-p3-e2a-acceptance.md) (E2a media block, ceiling persisted), [117](../evidence/my-ai-migration/parity/117-p3-e3-deferred-ceilings.md) + [159](../evidence/my-ai-migration/parity/159-p3-e8-deferred-ceilings.md) (registration).

### Capture Wave-3 Flutter-refs

After Phase-3 ticket **E1a** lands (the new Wave-3 capture path), drive a Wave-3 capture explicitly:

```bash
# Wave-3 = distinct block (E1a): 05 usedcar-detail, 08 settings, 09 login-password,
# 14 post-detail, 15 music-list, 19 friend-list, 25 pay-list. Not an alias for
# `loggedin` — `CAPTURE_SET=all` still runs wave1+wave2+loggedin+wave3.
CAPTURE_SET=wave3 ./scripts/flutter-ref-capture.sh --update
```

`CAPTURE_SET=wave3` is a **distinct** capture block (E1a): usedcar-detail / settings / login-password / post-detail / music-list / friend-list / pay-list (not an alias for `loggedin`). `CAPTURE_SET=all` still runs wave1+wave2+loggedin+wave3. The `--update` flag locks actuals into `goldens/flutter-ref/baseline/`. Reuses settle / paywall / login helpers from Soft Gate work.

**Wave3 capture must not abort on the first UNRELIABLE stem** (P3-E1a2 / E1a3 harden): every `wave3_capture` invocation is invoked as `wave3_capture … || true` so a size-gate failure or a missing UI entry on one stem does not poison the rest of the wave. Stems that stay under `WAVE3_MIN_BYTES` (default 100000) leave their `WAVE3_OK_<stem>` flag at `0`, and the `--update` lock step at the bottom of the script explicitly **skips locking** those baselines (`skip locking unreliable $bn (wave3 gate)`). Deferral evidence: [101](../evidence/my-ai-migration/parity/101-p3-e1a2-acceptance.md) and [105](../evidence/my-ai-migration/parity/105-p3-e1a3-acceptance.md).

### Add a MAP pair

`scripts/golden-vs-flutter-diff.sh` owns the Cross-Source MAP. To gate a new pair end-to-end:

1. Land the Kuikly actual under `goldens/android/actual/<kuik-stem>.png` and the Flutter ref under `goldens/flutter-ref/baseline/<flutter-stem>.png` (capture via `flutter-ref-capture.sh`).
2. Add an entry to the `MAP` array in `golden-vs-flutter-diff.sh`:
   ```bash
   MAP=(
     "02-main-home=02-flutter-main-home"
     # ... existing pairs ...
     "XX-kuikly-page=XX-flutter-page"   # ← new
   )
   ```
3. Run the cross-source gate; the script masks top/bottom (status bar + home indicator) and reports `RMSE_rel` against `GOLDEN_RMSE_MAX` (default **0.22**):
   ```bash
   GOLDEN_RMSE_MAX=0.22 ./scripts/golden-vs-flutter-diff.sh
   ```
   Override per-stem during calibration with `GOLDEN_RMSE_MAX=0.30 ...`; **never** raise the default above 0.22 without an evidence doc — Phase-2 hard gate is calibrated to it.
4. The script exits non-zero if any mapped pair exceeds `RMSE_MAX`; treat it as a Phase-3 acceptance gate (capture-quality / asset-mismatch / density-gap root cause before relaxing).

### `02b-main-home-logged-in` — promoted (home assets)

`02b-main-home-logged-in=02b-flutter-main-home-logged-in` is now **in MAP** (see `scripts/golden-vs-flutter-diff.sh`). History:

- Pre-E1b: Flutter-ref is photo-dense (~973 KB on Pixel_7_Pro) vs Kuikly's glyph/solid placeholders (~208 KB) → measured RMSE_rel ≈ **0.250** under the no-image-SDK ceiling. Originally Android self-lock only (see `91`).
- Post-E1b: home photo/icon assets landed under `app-shared/src/commonMain/assets/common/home/` ([102](../evidence/my-ai-migration/parity/102-p3-e1b-acceptance.md)); Kuikly `02b` re-captured and RMSE_rel dropped to ≈**0.192** — under the 0.22 default → promoted to MAP12.

If a future Flutter-ref or Kuikly capture regresses back above 0.22, **drop the pair** from `golden-vs-flutter-diff.sh` `MAP` and re-classify as Android self-lock until the next asset/polish pass.

### Phase-3 evidence stubs

- [86 — P2 Soft Gate S1 PASS](../evidence/my-ai-migration/parity/86-p2-s1-soft-gate-pass.md) — baseline MAP 9/9 + iOS/Ohos self-lock (consumed by Phase-3 as the starting point)
- [91 — P2 Soft Gate S2 PASS](../evidence/my-ai-migration/parity/91-p2-s2-soft-gate-pass.md) — Android self-lock + `02b` ceiling rationale (consumed by Phase-3 as the pre-promotion `02b` deferral note source)
- [95 — P3-E1a acceptance (Flutter-ref Wave3 capture expand)](../evidence/my-ai-migration/parity/95-p3-e1a-acceptance.md) — `CAPTURE_SET=wave3` distinct block; new 05/08/09/14/15/19/25 stems
- [96 — P3-E1d acceptance (Runbook Phase-3)](../evidence/my-ai-migration/parity/96-p3-e1d-acceptance.md) — initial Phase-3 runbook expansion; wave3 wording fix to match E1a
- [97 — P3-E1e acceptance (Android settle bump Wave3)](../evidence/my-ai-migration/parity/97-p3-e1e-acceptance.md) — 5s settle for 05/08/09/14/15/19/20/25/26 stems
- [98 — P3-E1f acceptance (MAP Wave3 pairs)](../evidence/my-ai-migration/parity/98-p3-e1f-acceptance.md) — `golden-vs-flutter-diff.sh` MAP appends 7 Wave3 pairs; first `CAPTURE_SET=wave3 --update` produced blanks for 05/08/15/19/25 → baselines removed
- [99 — P3-E1c acceptance (Inventory refresh)](../evidence/my-ai-migration/parity/99-p3-e1c-acceptance.md) — INVENTORY post-Phase-2 vocabulary (MAP-green / flutter-ref-pending / self-lock / structure / missing / out)
- [100 — P3-E1 partial gate (MAP 10 + Wave3 notes)](../evidence/my-ai-migration/parity/100-p3-e1-partial-gate.md) — first Wave3 RMSE snapshot; `09-login-password` promoted, `14-post-detail` deferred
- [101 — P3-E1a2 acceptance (Flutter Wave3 harden)](../evidence/my-ai-migration/parity/101-p3-e1a2-acceptance.md) — `wave3_capture` + `WAVE3_MIN_BYTES=100000` + per-stem `WAVE3_OK_*` skip-lock on `--update`
- [102 — P3-E1b acceptance (Home 02b photo assets)](../evidence/my-ai-migration/parity/102-p3-e1b-acceptance.md) — home photo/icon assets that drove `02b` RMSE from ~0.250 to 0.192 → promotion
- [103 — P3-E2a acceptance (PostDetail media assets)](../evidence/my-ai-migration/parity/103-p3-e2a-acceptance.md) — `14-post-detail` media block; RMSE ceiling still ~0.34 vs Flutter photo density → remain deferred
- [104 — P3-E1 gate (MAP 12)](../evidence/my-ai-migration/parity/104-p3-e1-gate-map12.md) — Phase-3 hard MAP gate: 05/09 added, `02b` promoted, 08/14/15/19/25 deferral notes locked
- [105 — P3-E1a3 acceptance (music/friend/pay UI-tap fallback)](../evidence/my-ai-migration/parity/105-p3-e1a3-acceptance.md) — UI-tap fallback for 15/25; `19-friend-list` Flutter-ref stays UNRELIABLE (Flutter `FriendPage` is a stub)
- [112 — P3-E2 gate (MAP 13)](../evidence/my-ai-migration/parity/112-p3-e2-gate-map13.md) — Phase-3 hard MAP gate: `25-pay-list` promoted (RMSE_rel 0.159 via AllServices→会员续费 UI tap); 08/14/15/19 deferral notes refreshed
- [125 — P3-E3 gate (MAP 14)](../evidence/my-ai-migration/parity/125-p3-e3-gate-map14.md) — Phase-3 hard MAP gate: `15-music-list` promoted (RMSE_rel 0.145, Flutter-ref AllServices→音频列表 multi-scroll E3b2 + Kuikly E3e 72dp row); 08 (0.237) / 14 / 19 deferral notes refreshed
- [140 — P3-E4 gate (MAP 15)](../evidence/my-ai-migration/parity/140-p3-e4-gate-map15.md) — Phase-3 hard MAP gate: `08-settings` promoted (RMSE_rel **0.103**); prior Flutter `08-flutter-settings.png` baseline was a **Home** mis-lock (459KB Home frame), reinstall + privacy dismiss + guest Mine UI-tap to「设置」yielded true Settings ref (230426B) — combined with E3a/E4e2 flatten crossed the 0.22 gate; `14-post-detail` (photo density) and `19-friend-list` (Flutter stub) remain deferred
- [148 — P3-E7 gate (MAP 17)](../evidence/my-ai-migration/parity/148-p3-e7-gate-map17.md) — Phase-3 hard MAP gate: `18-video-detail` promoted (RMSE_rel **0.211**) + `21-classroom-list` promoted (RMSE_rel **0.141**) after Wave4 Flutter-ref lock + 2026-09-18 Kuikly recapture; `17-video-list` deferred (Flutter-ref Home mis-lock — [149](../evidence/my-ai-migration/parity/149-p3-e7f-acceptance.md); recapture after E8a content gate), `23-live-list` / `26-pay-confirm` deferred (Flutter-ref UNRELIABLE); `14-post-detail` (photo density) and `19-friend-list` (Flutter stub) remain deferred
- [156 — P3-E8b acceptance (video-list true Flutter-ref)](../evidence/my-ai-migration/parity/156-p3-e8b-acceptance.md) — recaptured `17-flutter-video-list` after the E7-era Home mis-lock, unblocking the pair for promotion
- [158 — P3-E8g gate (MAP 18)](../evidence/my-ai-migration/parity/158-p3-e8-gate-map18.md) — Phase-3 hard MAP gate: `17-video-list` promoted (RMSE_rel **0.154**) after E8a content gate + E8b true Flutter-ref; all 18 pairs under `GOLDEN_RMSE_MAX=0.22`; `14-post-detail` (photo density), `19-friend-list` (Flutter stub), `23-live-list` / `26-pay-confirm` (Flutter-ref UNRELIABLE) remain deferred
- [172 — P3-E9g unlock (MAP 20)](../evidence/my-ai-migration/parity/172-p3-e9g-friend-live-unlock.md) — `19-friend-list` / `23-live-list` promoted (RMSE_rel **0.032** / **0.032**) after stub flatten + true Flutter stub refs; `14` / `26` PASS-out; deferred blocking list empty
- [175 — P5 Soft Close](../evidence/my-ai-migration/parity/175-p5-soft-close.md) — structure source-diff queue drained; hard MAP still **20/20**
- [176 — P6-E2b docs](../evidence/my-ai-migration/parity/176-p6-e2b-docs.md) — INVENTORY + runbook Phase-6 stem table; MAP unlock pending
- [177 — P6-E2-GATE MAP 32](../evidence/my-ai-migration/parity/177-p6-e2-gate-map32.md) — Wave5 stems 27–38 unlocked; hard MAP **32/32**
- [178 — P6 Soft Close](../evidence/my-ai-migration/parity/178-p6-soft-close.md) — Phase-6 queue drained; out-of-scope unchanged
- [179 — P7-E1 live-detail unlock](../evidence/my-ai-migration/parity/179-p7-e1-live-detail-unlock.md) — `24-live-detail` MAP; splash PASS-out; hard MAP **33/33**
- [182 — P8-E2-GATE MAP 35](../evidence/my-ai-migration/parity/182-p8-e2-gate-map35.md) — works 40/41 unlocked; stem 39 deferred
- [183 — P8 Soft Close](../evidence/my-ai-migration/parity/183-p8-soft-close.md) — missing/self-lock drained; hard MAP **35/35** (superseded by 184)
- [184 — P8-E3 MAP 36](../evidence/my-ai-migration/parity/184-p8-e3-gate-map36.md) — `39-dubbing-home` unlocked (RMSE_rel **0.214**)

## Phase-8 — Missing drain

**Status:** **PASS**. Implemented `DubbingHome` / `DubbingWorkList` / `DubbingWorkDetail`. Hard MAP **36/36** ([184](../evidence/my-ai-migration/parity/184-p8-e3-gate-map36.md)). Inventory missing **0**.

## Phase-6 — Structure pages → Flutter-ref + MAP

**Status:** **PASS** — hard Cross-Source MAP **32/32** ([177](../evidence/my-ai-migration/parity/177-p6-e2-gate-map32.md)). All Wave5 stems **27–38** unlocked (RMSE_rel ≤ 0.22).

| Kuikly stem | Flutter ref | Route | RMSE_rel |
|---|---|---|---|
| `27-all-services` | `27-flutter-all-services` | `/home/all_services` | ≈0.147 |
| `28-register` | `28-flutter-register` | `/register` | ≈0.068 |
| `29-music-now-playing` | `29-flutter-music-now-playing` | `/music/now_playing` | ≈0.039 |
| `30-strategy` | `30-flutter-strategy` | `/home/strategy` | ≈0.144 |
| `31-hot-rank-detail` | `31-flutter-hot-rank-detail` | `/home/hot_rank_detail` | ≈0.150 |
| `32-personalized-settings` | `32-flutter-personalized-settings` | `/mine/personalized_settings` | ≈0.089 |
| `33-learning-report` | `33-flutter-learning-report` | `/home/learning_report` | ≈0.163 |
| `34-check-in-mall` | `34-flutter-check-in-mall` | `/home/check_in_mall` | ≈0.161 |
| `35-deal-invoice-upload` | `35-flutter-deal-invoice-upload` | demo→FAB (`/settings/deal_invoice_demo`) | ≈0.043 |
| `36-classroom-gift-claim` | `36-flutter-classroom-gift-claim` | `/classroom/gift/claim` | ≈0.106 |
| `37-classroom-video-detail` | `37-flutter-classroom-video-detail` | `/classroom/video/detail` | ≈0.127 |
| `38-community-publish` | `38-flutter-community-publish` | `/community/publish` | ≈0.035 |

```bash
# Recapture / re-gate
CAPTURE_SET=wave5 ./scripts/flutter-ref-capture.sh --update   # 35: prefer demo→FAB if upload deeplink sticks on Home
./scripts/golden-android-capture.sh
GOLDEN_RMSE_MAX=0.22 ./scripts/golden-vs-flutter-diff.sh
```

**Note:** Direct `xiaomao://app/settings/deal_invoice/upload` may leave Home; lock `35` via `settings/deal_invoice_demo` then tap「上传成交发票」.

Out of scope unchanged: bfui / short-video SDK / homework missing routes / web / H5.

## Masks

- Top: `MASK_TOP_PX` default 80 (status bar)
- Bottom: `MASK_BOTTOM_PX` default 40 (home indicator)
- Threshold: `GOLDEN_PIXEL_THRESHOLD` default 50000 — calibrate after first Home pair

## Privacy / login on Flutter

Cold start may show privacy dialog. For stable refs: accept once on device, or document a debug skip when available. Re-lock refs after mock data changes.
