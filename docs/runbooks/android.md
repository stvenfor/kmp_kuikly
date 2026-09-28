# Android smoke

## One-shot (recommended)

```bash
./scripts/run.sh android
# ./scripts/run.sh android --page Login --avd Pixel_7_Pro
```

Cold start default: **Splash** (`KuiklyRenderActivity` when `pageName` empty).

## Manual

```bash
./gradlew :androidApp:assembleDebug
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName Login
```

## Expect

- Splash → Login or Main (session)
- Demo pages (HelloWorld / Home Demo Map) only via explicit `--page` / Intent

## Evidence dir

`docs/evidence/02-android/`
