# Android smoke

## Build

```bash
./gradlew :androidApp:assembleDebug
```

## Run (emulator or device)

```bash
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity
# or explicit page:
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName HelloWorld
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName Login
```

## Expect

- HelloWorld: “Hello, kmp_kuikly!” + link to Login
- Login → FeedList (Mock); cycle scenario on FeedList

## Evidence dir

`docs/evidence/02-android/`
