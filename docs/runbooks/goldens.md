# Per-Platform Golden runbook (first Product Vertical Slice)

## Contract

- Strategy: **Per-Platform Golden** (ADR-0005) — each platform locks its own baselines; not Flutter pixel parity.
- Gate platforms: **Android + iOS + HarmonyOS**. H5 out of first slice.
- Closed pages: Splash, Main (Home shell), Login (OTP default + password mode), UsedCarList, UsedCarDetail (+ privacy overlay when consent revoked),
  Main「我的」guest, Main「我的」logged-in, Settings, SearchPage.

## Golden page list

| Golden | Page | pageData | State |
|--------|------|----------|-------|
| `01-splash` | Splash | — | default |
| `02-main-home` | Main | — | Home tab, guest |
| `03-login` | Login | — | default (mock OTP hint visible) |
| `04-usedcar-list` | UsedCarList | — | default (Success scenario) |
| `05-usedcar-detail` | UsedCarDetail | `{"id":"1"}` | default |
| `06-main-me-guest` | Main | `{"tab":"Me"}` | 「我的」tab, guest (fresh process) |
| `07-main-me-logged-in` | Main | `{"tab":"Me","mockLogin":"1"}` | 「我的」tab, FakeAuth OTP logged-in |
| `08-settings` | Settings | — | guest (direct deep-link) |
| `09-login-password` | Login | `{"mode":"password"}` | password mode (Slice-03) |

### Priming: how pageData reaches the Kuikly page

`MainPage.willInit` reads `pageData.params`:

- `tab=Me` — seeds `MainTabLaunch` so Main opens on 「我的」.
- `mockLogin=1` — **debug-only golden priming** (`ponytail: golden priming`): performs one
  FakeAuth OTP login (`13400000000` / `123456`) so captures can render the logged-in Me state.
  Session lives in memory only; every capture force-stops / terminates the app first, so
  `06-main-me-guest` stays guest and `07-main-me-logged-in` logs in fresh each run.

`LoginPage` reads `pageData.params.optString("mode") == "password"` → opens in password mode
(`09-login-password`); without it Login stays on the OTP default (`03-login`).

Per-platform transport:

| Platform | Mechanism |
|----------|-----------|
| Android | `adb shell am start --es pageName <Page> --es pageData '<json>'` |
| iOS | `SIMCTL_CHILD_KUIKLY_PAGE` / `SIMCTL_CHILD_KUIKLY_PAGE_DATA` env vars → `AppDelegate` → `KuiklyRenderViewController(pageData:)` |
| HarmonyOS | `aa start --ps pageName <Page> --ps pageData '<json>'` → `EntryAbility` → `AppStorage` → `Index.ets` JSON.parse |

Logged-in Settings (logout button visible) is reached in-app from Me → 设置 and is **not**
a named golden yet; add an explicitly-named state golden (e.g. `08b-settings-logged-in.png`)
if a regression gate on that state is needed.

## Locked device profiles

| Platform | Profile | Notes |
|----------|---------|--------|
| Android | `Pixel_7_Pro` AVD | **Locked** — `goldens/android/baseline/` + diff green |
| iOS | `iPhone 16` simulator | **Locked** — `goldens/ios/baseline/` + diff green |
| HarmonyOS | `127.0.0.1:5557` (Connected emulator) | **Locked 2026-09-16** — `goldens/ohos/baseline/` + diff green |

## Android (scripts ready)

```bash
# device online required
./scripts/run-android.sh --page Splash   # install once
./scripts/golden-android-capture.sh      # → goldens/android/actual/ (01–08)
./scripts/golden-android-capture.sh --update   # lock baselines
./scripts/golden-android-diff.sh         # fail ≠ 0 on mismatch
```

Package: `com.example.kuikly`. Deep-link via `--es pageName <Page>` (+ `--es pageData '<json>'`).

## iOS / HarmonyOS (scripts ready, same flow)

```bash
./scripts/run-ios.sh --page Splash       # install once (also builds app w/ pageData support)
./scripts/golden-ios-capture.sh          # → goldens/ios/actual/ (01–08)
./scripts/golden-ios-diff.sh

./scripts/run-ohos.sh                    # install once
./scripts/golden-ohos-capture.sh          # → goldens/ohos/actual/ (01–10)
./scripts/golden-ohos-diff.sh
```

Both capture scripts require the app to be installed; iOS uses
`SIMCTL_CHILD_KUIKLY_PAGE[_DATA]` env, OHOS uses `aa start --ps`.

## Locking order (Slice-02)

Baseline locks for `06`–`08` happen in tickets 06 (Android), 07 (iOS), 08 (OHOS) with
devices online; script changes must land first (this runbook). Never `--update` baselines
outside a golden-lock ticket approved by Orchestrator.

## Slice-03 addition (`09-login-password`)

Capture scripts now emit `09-login-password` (Login, pageData `{"mode":"password"}`) on all
three platforms; `03-login` remains the OTP default regression. Diff scripts pick up the
new baseline automatically once locked. Per ticket 03, **no baseline lock required here**
(locking still belongs to golden-lock tickets 06–08 flow, with devices online and
Orchestrator approval).

## Slice-04 addition (`10-search`)

Capture scripts now emit `10-search` (SearchPage, deep-linked by `pageName=Search`, no
pageData) on all three platforms. `02-main-home` still captures `Main` unchanged, but now
renders with the Home search chrome (Slice-02 SearchBar) — its baseline must be re-locked
in device-gate tickets 05–07, not here. Per ticket 04, **no baseline lock / `--update` in
this ticket**; locking still belongs to the golden-lock ticket flow with devices online
and Orchestrator approval.

## Mock scenarios

Toggle in-app on Home (“切换 Mock 场景”) or set before capture. Baselines should be locked under **Success** unless a state-specific golden is named (e.g. `04-usedcar-list-empty.png`) — extend checklist explicitly when adding state goldens.
