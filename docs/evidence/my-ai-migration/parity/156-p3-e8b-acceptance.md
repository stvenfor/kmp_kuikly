# P3-E8b Acceptance — Recapture true 17 Flutter video-list

**Verdict: PASS**

- Wave4 UI-tap fallback found `视频列表`, shot size **369668**.
- Content gate: `视频 signal present, no Home needle — OK`.
- Baseline locked: `goldens/flutter-ref/baseline/17-flutter-video-list.png` SHA `6e5f8d41…` (was Home `d97749b8…` ~1.0MB).
- Deeplink path correctly rejected (size ~60KB under gate → UI fallback).

Refs: `parity/logs/orch-e8b-recapture.log`
