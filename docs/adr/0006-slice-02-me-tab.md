# Slice-02: Me (Mine) tab root + minimal Settings

**Status:** accepted (all recommendations auto-applied 2026-09-16)  
**Parent:** ADR-0005  
**Spec:** `.scratch/my-ai-migration/slice-02/spec.md`

## Context

Slice-01 left Chat / Community / Me as explicit placeholders. Migration Source mounts **MinePage** as the「我的」main tab (`module_settings`). Reviewers see the placeholder as the loudest shell gap.

## Decision

Slice-02 replaces the Me placeholder with a **Mine root** that matches the Migration Source structure (header / quick services / function section / menu list) under Mock, plus a **minimal Settings** secondary page for logout and return navigation. Guest vs logged-in profile states are required. Most menu/quick-service taps show an explicit later-slice affordance; **登录** and **设置** are real.

## Consequences

- Reuse `feature-home` Main tab content, `feature-auth` OTP modal, `core-data` Mock — no new Feature Gradle module in this slice.
- Goldens add Me guest, Me logged-in, Settings; Slice-01 key pages remain regression.
- Completion still requires Android + iOS + HarmonyOS Per-Platform Golden PASS.
- Chat / Community stay placeholders; full Settings (theme/env/language/deal-invoice/personalized) wait for later slices.
