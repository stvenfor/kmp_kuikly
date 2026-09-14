# h5App

Kuikly H5 Shell — aligned with [KuiklyUI `h5App` @ 2.16.0](https://github.com/Tencent-TDS/KuiklyUI/tree/2.16.0/h5App), using published Maven web render (`com.tencent.kuikly-open.core-render-web`) instead of monorepo `project(":core-render-web:*")`.

Business module path: **`app-shared`** (packs `nativevue2.js`).

## Quick run

See [`docs/runbooks/h5.md`](../docs/runbooks/h5.md):

```bash
./gradlew :shared:packLocalJsBundleDebug
./gradlew :h5App:jsBrowserDevelopmentRun -t
# browser → http://localhost:8080/?page_name=HelloWorld
```
