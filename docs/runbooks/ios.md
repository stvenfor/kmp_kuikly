# iOS smoke

## One-shot (recommended)

```bash
./scripts/run.sh ios --page Home --sim "iPhone 16"
# after pods are warm:
./scripts/run.sh ios --page Login --skip-pods
```

## Manual

```bash
./gradlew :app-shared:generateDummyFramework
cd iosApp
xcodegen generate
pod install
open iosApp.xcworkspace
```

`AppDelegate` reads `KUIKLY_PAGE` (script sets `SIMCTL_CHILD_KUIKLY_PAGE`).

## Blockers

- Needs Xcode + CocoaPods; `OpenKuiklyIOSRender` pod clones from GitHub (network).
- Visual evidence may be deferred if `pod install` / signing fails.
