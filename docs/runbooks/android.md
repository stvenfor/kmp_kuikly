# Android smoke

## One-shot (recommended)

```bash
./scripts/run.sh android --page Home
# ./scripts/run.sh android --page Login --avd Pixel_7_Pro
```

## Manual

```bash
./gradlew :androidApp:assembleDebug
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName HelloWorld
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName Login
```

## Expect

- HelloWorld: “Hello, kmp_kuikly!” + link to Login
- Login → FeedList (Mock); cycle scenario on FeedList

## Evidence dir

`docs/evidence/02-android/`
