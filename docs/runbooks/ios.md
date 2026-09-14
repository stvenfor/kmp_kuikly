# iOS smoke

## Generate + pods

```bash
./gradlew :app-shared:generateDummyFramework
cd iosApp
xcodegen generate   # requires xcodegen
pod install
open iosApp.xcworkspace
```

## Run

Select a simulator, Run `iosApp`. Open page `HelloWorld` (or whatever the host defaults to).

## Blockers

- Needs Xcode + CocoaPods; `OpenKuiklyIOSRender` pod clones from GitHub (network).
- Visual evidence on this machine may be deferred if `pod install` / signing fails — note in ticket Comments.
