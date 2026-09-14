# Embed contract

External native hosts should **not** fork Shell Apps long-term. Depend on the KMP business artifact (`:app-shared`).

## Host must

1. Depend on the shared Kuikly business module (AAR / framework / JS bundle as published).
2. Embed **KuiklyRender** per platform ([docs](https://kuikly.tds.qq.com/QuickStart/common.html)).
3. Register adapters (router, image, font, thread, exception) — see `androidApp/.../adapter/`.
4. Register bridge modules (`HRBridgeModule`, share, …).
5. Open pages by **Page name** (see [demo-map.md](demo-map.md)), e.g. Android:

```bash
adb shell am start -n com.example.kuikly/.KuiklyRenderActivity --es pageName Home
```

## Host must not

- Put business UI in the Shell.
- Depend on `platform-*-android` impls from Features (Features use expect APIs only).

## v1 delivery

AOT / bundled with the app. Dynamicization Seam reserved (ADR-0003).
