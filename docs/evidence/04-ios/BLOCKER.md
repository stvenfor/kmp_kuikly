# iOS evidence blocker

`pod install` fails downloading `OpenKuiklyIOSRender` from `https://github.com/Tencent-TDS/KuiklyUI.git` (tag `2.16.0`):

- `Error in the HTTP2 framing layer` / `Couldn't connect to github.com port 443`

Dummy framework + xcodegen otherwise OK. Retry when GitHub network is reachable; Podfile path is now `../app-shared`.
