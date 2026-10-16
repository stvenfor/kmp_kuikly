# Phase-2 Plan — Cross-Source Visual Parity

**ADR:** [0018](../../docs/adr/0018-cross-source-visual-parity.md)  
**Program:** [`.scratch/my-ai-migration/PROGRAM.md`](../../.scratch/my-ai-migration/PROGRAM.md)  
**Source:** `/Users/mac/Desktop/github/my_ai_project`  
**Settled:** all recommended defaults (2026-09-17)

---

## 1. Why Phase-1 is not “复刻”

| Phase-1 delivered | What reviewers need |
|-------------------|---------------------|
| Self-locked 26 goldens | Match **Flutter** screenshots |
| Shell / thin list→detail | Full Home dashboard, Mine density, real chrome |
| Domain stubs exist | Same section order, labels, colors as source |

Phase-1 remains useful engineering (nav, Mock, three-platform pipeline). It is **not** visual Destination.

---

## 2. Settled decisions (recommended)

1. **Truth:** Flutter Android screenshots = visual source of truth.  
2. **Hard gate:** Kuikly Android vs `goldens/flutter-ref/` (AE + status-bar mask).  
3. **Soft gate:** iOS/Ohos self-lock + structure checklist.  
4. **Scope:** All **business** routes (~52), not bfui demos; phased by visual impact.  
5. **“一致”:** layout, copy, section order, accent/system colors, density — not cross-engine byte identity.  
6. **Process:** Wave 0 infra → Wave 1 Home+Mine → Wave 2 thicken existing slices → Wave 3 remaining inventory.  
7. **Roles:** Orchestrator assigns/accepts + owns ref/golden; WorkBuddy implements Compose UI.  
8. **Data:** Mock still gate; enrich seeds to match Flutter mock strings.

---

## 3. Tooling (Wave 0)

| Artifact | Purpose |
|----------|---------|
| `goldens/flutter-ref/baseline/*.png` | Flutter reference locks |
| `scripts/flutter-ref-capture.sh` | Drive Flutter on `emulator-5554`, dump pages |
| `scripts/golden-vs-flutter-diff.sh` | Compare Kuikly Android actual ↔ Flutter ref (mask + AE) |
| `docs/runbooks/visual-parity.md` | Device profile, mask rects, thresholds |
| `.scratch/my-ai-migration/parity/INVENTORY.md` | Route → Flutter file → Kuikly page → status |

**Device lock (recommended):** Android `emulator-5554` / Pixel_7_Pro, same for Flutter and Kuikly Android captures.

**Mask (recommended):** top status bar strip + bottom home indicator; ignore anti-alias noise via AE threshold (start `GOLDEN_PIXEL_THRESHOLD` calibrated per page class: chrome ~0.5% pixels, dense lists higher).

---

## 4. Wave plan

### Wave 0 — Infra (start now)
- [ ] Page inventory from `RoutePath` + Main tabs  
- [ ] Flutter ref capture for Main/Home/Mine/Login/Chat/Community smoke set  
- [ ] Diff script + mask  
- [ ] Side-by-side evidence folder `docs/evidence/my-ai-migration/parity/`

### Wave 1 — Loudest visual gaps (block everything else)
| Slice | Flutter truth | Kuikly target |
|-------|---------------|---------------|
| P2-01 | `HomePage` dashboard (greeting, search, top tabs, banner, feature grid, quick actions, metrics, …) | Replace shell `HomeShellTab` |
| P2-02 | `MinePage` guest + logged-in | Match header/stats/services/functions/menu density |
| P2-03 | `IosTabBar` chrome | Main bottom bar visual parity |

### Wave 2 — Re-thicken Phase-1 pages against Flutter refs
Login (OTP+password), Search, UsedCar list/detail, Chat list/detail, Community feed/detail, Music list+mini, Video/dubbing, Friend, Classroom, Live, Pay — each only **PASS** when Android↔Flutter-ref AE green.

### Wave 3 — Remaining inventory
Home subpages (all_services, dubbing_feed, strategy, learning_report, …), classroom deep routes, short video, settings subpages — inventory-ordered; skip bfui.

---

## 5. Acceptance template (every Phase-2 slice)

1. Flutter ref PNG locked for route+state.  
2. Kuikly Android capture for same deep-link/state.  
3. `golden-vs-flutter-diff.sh` PASS (masked).  
4. iOS + Ohos self-lock regression PASS.  
5. Orchestrator visual note in `docs/evidence/my-ai-migration/parity/P2-xx-*.md`.  
6. WorkBuddy must **not** invent simplified shells for in-scope screens.

---

## 6. Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Kuikly missing Flutter widgets (blur tab bar, EasyRefresh) | Approximate with Compose primitives; document ceiling in `ponytail:` if unavoidable |
| Font / density drift | Prefer structure+color+copy gate; AE threshold not zero |
| Scope explosion (72 routes) | Inventory + waves; bfui out |
| Flutter capture flaky | Same emulator + scripted routes; freeze mock |

---

## 7. Immediate next actions

1. Land inventory + ADR/CONTEXT/PROGRAM updates.  
2. Implement Flutter ref capture for smoke set.  
3. Open P2-01 Home dashboard WorkBuddy fat ticket after refs exist.
