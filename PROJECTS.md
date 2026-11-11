# 项目列表

## Myz 手机端专用启动器

| 项目 | 内容 |
| --- | --- |
| 项目名称 | Myz 手机端专用启动器 |
| 定位 | 基于 Fold Craft Launcher（FCL）的 Minecraft: Java 版安卓启动器壳子，面向指定服务器定制 |
| 版本 | v1.0.0（versionCode 1345，与上游保持一致） |
| 包名 | `com.tungsten.fcl`（沿用上游应用 ID） |
| 许可 | GPL-3.0 |
| 作者 | 单声-SX（GitHub: [MONO-SX](https://github.com/MONO-SX)） |

本项目以 Fold Craft Launcher 为底座做定制，改动集中在界面外观（背景图、Logo、首页启动区、账户面板）、品牌信息（应用名、图标）以及启动参数默认值注入；版本管理、游戏启动、账户体系、下载与运行时安装等核心能力全部来自上游，未重写。

## 源码来源与参考

| 项目 | 仓库 | 关系 | 说明 |
| --- | --- | --- | --- |
| Fold Craft Launcher (FCL) | [FCL-Team/FoldCraftLauncher](https://github.com/FCL-Team/FoldCraftLauncher) | 直接 fork / 使用 | 本项目的上游。整个代码库（`:FCL` 模块、原生层 JNI、JRE 资源、fclcore 启动核心）直接继承，保留原始版权头与 GPL-3.0 许可；本项目只在其上做界面与品牌层改动。 |
| HMCL | [huanghongxun/HMCL](https://github.com/huanghongxun/HMCL) | 参考 / 间接 | FCL 的 fclcore 启动架构与 HMCL 同源（FCL 作者亦参与 HMCL），但本项目未直接引入 HMCL 的源码或依赖，构建产物中不含 HMCL 代码。 |
| PojavLauncher | [PojavLauncherTeam/PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher) | 参考 / 间接 | 安卓端 JVM 启动、原生库加载路线的参考实现。本项目未直接包含 PojavLauncher 代码，原生层（JNI、LWJGL natives、JRE 解包）使用 FCL 自带实现。 |
| Amethyst（Amethyst-Android） | — | 未使用 | 与本项目无代码或二进制依赖关系，仅作为同类安卓启动器了解过，未引用。 |
| Zalith Launcher | — | 未使用 | 与本项目无代码或二进制依赖关系，仅作为同类安卓启动器了解过，未引用。 |

> 表中「未使用」项不是依赖项，列出仅为说明技术路线来源，避免误解。

## 本项目相对上游的改动概要

- 界面：首页背景图与前景 Logo 层、开始游戏按钮（含版本号）、启动状态文字与进度条、账户面板布局与配色重绘。
- 品牌：应用名改为「Myz手机端专用启动器」，替换为自适应图标（含圆形与传统密度变体）。
- 启动参数：注入服务器默认地址，启动时自动带入 `--server` / `--port`（1.20 以下）或 `--quickPlayMultiplayer`（1.20 及以上）。
- 版本：`versionName` 调整为 1.0.0；`versionCode` 保持 1345 不变，以免影响覆盖安装与升级判断。

## 许可

本项目遵循 GPL-3.0，保留上游版权与许可声明，源码分发须同样以 GPL-3.0 提供。各上游项目的原作者与贡献者名单以对应仓库为准。
