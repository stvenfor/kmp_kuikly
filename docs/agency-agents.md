# Agency Agents 使用说明（本仓）

安装与角色映射见根目录 [AGENTS.md](../AGENTS.md)。

## 验收最低证据（与 Reality Checker 对齐）

对任意「可演示」工单，代理在宣称完成前应附上：

1. **命令证据**：相关 `./gradlew …` 或单测输出（通过）
2. **Android 视觉证据**：模拟器/真机截图或短录屏（登录竖切等）
3. **其它端**：按 `docs/runbooks/` 勾选清单（无 runbook 则票未完成文档部分）

无以上证据 → 状态保持 **NEEDS WORK**，不得写「已验收通过」。
