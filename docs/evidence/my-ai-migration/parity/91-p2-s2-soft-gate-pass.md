# P2 Soft Gate S2 — Android self-lock + MAP hygiene

**Date:** 2026-09-17  
**Verdict:** **PASS** (primary) / **02b MAP deferred** (ceiling)

## Primary goal — Android Per-Platform self-lock

After Phase-2 UI polish, `golden-android-diff` was **26/26 FAIL**.  
Orchestrator ran `golden-android-capture.sh --update` (with S2a size-poll) → **all Android goldens AE=00 PASS** (incl. new `02b-main-home-logged-in` stem).

## Cross-Source MAP

- Hard MAP remains **9/9 PASS** (incl. `12-chat-detail`).  
- `02b` briefly added then **deferred**: Flutter-ref ~973KB photo-dense vs Kuikly ~208KB glyphs → RMSE_rel≈**0.250** (>0.22) under no-image-SDK ceiling.  
- Soft polish S2e/S2f/S2g landed for greeting / banner / grid tones — re-add `02b` to hard MAP only when image assets exist or RMSE ≤0.22.

## WorkBuddy

| ID | Result |
|----|--------|
| S2a size-poll + 02b stem | PASS ([87](87-p2-s2a-acceptance.md)) |
| S2b MAP 02b (deferred) | PASS script ([88](88-p2-s2b-acceptance.md)); pair not hard-gated |
| S2c paywall dismiss | PASS ([89](89-p2-s2c-acceptance.md)) |
| S2d runbook | PASS ([90](90-p2-s2d-acceptance.md)) |
| S2e greeting | PASS ([92](92-p2-s2e-acceptance.md)) |
| S2f banner+grid | PASS ([93](93-p2-s2f-acceptance.md)) |
| S2g auth `dev-` id | PASS ([94](94-p2-s2g-acceptance.md)) |

## Destination note

Phase-2 hard gate + Soft Gate S1 (iOS/Ohos + chat-detail) + Soft Gate S2 (Android self-lock) **closed**. Optional backlog: promote `02b` when assets/RMSE allow.
