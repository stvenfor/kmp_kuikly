# HarmonyOS (Ohos) smoke

## Open

1. Open `ohosApp/` in **DevEco Studio**
2. Sync; if npmrc issues, re-sync `.npmrc`
3. **Signing Configs** (File → Project Structure → Signing Configs)
4. Run `entry` on device/emulator

## Versions

- `@kuikly-open/render`: see `ohosApp/entry/oh-package.json5` (aligned to Kuikly 2.16.0)
- Shared Ohos build file: `app-shared/build.ohos.gradle.kts`

## Blockers

- Requires DevEco + signing; not runnable from plain Gradle CI on macOS without Ohos SDK.
