# P3-E8a Acceptance — Wave4 video-list content gate

**Verdict: PASS**

- `video_list_content_ok()` requires `视频` signal and rejects Home greetings.
- Deeplink size-OK Home zeros `WAVE4_OK_17` and falls through to `ui_capture_video_list`.
- UI-tap path applies the same content reject before success.
- `bash -n` OK. No golden edits.

Next: E8b recapture on `emulator-5554`.
