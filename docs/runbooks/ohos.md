# HarmonyOS (Ohos) smoke

## One-shot (CLI best-effort)

```bash
./scripts/run.sh ohos
# or open IDE when link/signing is required:
./scripts/run.sh ohos --open-deveco
```

Script flow: find/copy `libshared.so` → `hvigor` assemble HAP → `hdc install` → start `EntryAbility`.

Cold start default page in `Index.ets`: **Splash** (not HelloWorld).

## DevEco (authoritative)

1. Open `ohosApp/` in **DevEco Studio**
2. Sync; if npmrc issues, re-sync `.npmrc`
3. **Signing Configs** (File → Project Structure → Signing Configs)
4. Run `entry` on device/emulator

## Versions

- `@kuikly-open/render`: see `ohosApp/entry/oh-package.json5` (aligned to Kuikly 2.16.0)
- Shared Ohos build file: `app-shared/build.ohos.gradle.kts`

## Blockers

- Requires DevEco + signing for a full green path.
- After physical modules, `-c settings.ohos.gradle.kts` may fail if `core-data` pulls Android plugins — use DevEco or a prebuilt `libshared.so` until ohos settings are slimmed.
