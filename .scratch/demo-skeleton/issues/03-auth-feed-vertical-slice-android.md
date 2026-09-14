# 03 — Auth + Feed Vertical Slice (Android)

**What to build:** On Android, a demoable path: Mock login → session → one feed list/detail with success/empty/error switches, Compose Track, plus common unit tests.

**Blocked by:** 02 — Four-platform smoke

**Agency role:** Mobile App Builder; Test Results Analyzer on tests

**Status:** done

- [x] User can log in with Mock Backend and see authenticated home entry into feed
- [x] Feed list and detail render; Mock scenarios Success / Empty / Error are switchable
- [x] Pages use Compose Track (not Legacy DSL)
- [x] common unit tests cover Mock engine + auth/feed repositories and pass
- [x] Evidence: Android screenshots of login + list + one error/empty state; test command output

## Comments

### Mobile App Builder
- Packages under `shared`: `data/mock`, `data/auth`, `data/feed`, `pages/{Login,FeedList,FeedDetail}`
- `HelloWorld` links to Login; `[CYCLE SCENARIO]` toggles MockBackend
- Bridge: `pageData`/`pageName` aligned for Android `KRBridgeModule`
- Fix: do not call `bridgeModule` in `willInit` (module not registered yet)

### Test Results Analyzer
`./gradlew :shared:testDebugUnitTest` — `MockAuthFeedTest` **5 tests, 0 failures**

### Evidence Collector
- `docs/evidence/03-android/03-login.png` — Login (Mock)
- `docs/evidence/03-android/03-feed-success.png` — list Success
- `docs/evidence/03-android/03-feed-empty.png` — Empty list
- `docs/evidence/03-android/03-feed-error.png` — Error: mock feed error

### Reality Checker
**PASS** for ticket 03. Unblocks 04∥05∥06 and 07.
