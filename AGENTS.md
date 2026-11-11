# AGENTS.md

本文件面向在本仓库工作的 AI/人类协作者。上游仓库没有 AGENTS.md / CLAUDE.md，本文件为二次开发项目新增，首次引入。

## 1. 项目定位

- 本项目是 **mc.xxx.com 的专用 Minecraft Java 版安卓客户端**（“壳子”），**不是**通用启动器。
- 所有 UI 文案、默认配置、启动参数、服务器入口、资源与品牌化改动，均以“让玩家一键进入 mc.xxx.com 服务器”为目标。
- 上游为 Fold Craft Launcher（FCL），协议 **GPL-3.0**，必须保留原作者版权声明与许可文件，二次分发须满足 GPL-3.0。
- 当前阶段只做工程地基与方案设计，**不做** JVM 启动核心、渲染器、原生库相关改动。

## 2. 技术架构（实际目录）

FCL = HMCL 核心（`fclcore`，移植自 `org.jackhuang.hmcl`）+ PojavLauncher/Amethyst 后端（JVM 启动与渲染）。

```
FCLShell/                             # 本地仓库目录名（上游仓库名 FoldCraftLauncher）
├── FCL/                              # 唯一 Application 模块（com.android.application）
│   ├── build.gradle.kts              # 模块构建配置：compileSdk 35 / minSdk 26 / targetSdk 34 / NDK 27.0.12077973
│   └── src/
│       ├── main/
│       │   ├── java/com/tungsten/
│       │   │   ├── fcl/              # FCL 业务与 UI：activity、ui、game、setting、control、fragment…
│       │   │   ├── fclcore/          # HMCL 核心移植：launch、game、auth、download、mod、task、event、util
│       │   │   ├── fclauncher/       # Pojav/Amethyst 后端 Java 桥：FCLauncher、bridge/FCLBridge、VMLauncher
│       │   │   ├── fcllibrary/       # 组件库：基类、主题、文件浏览器、崩溃处理
│       │   │   └── mio/              # 新代码层：MioLibPatcher / MioLaunchWrapper / Wrapper
│       │   ├── jni/                  # C/C++ 原生代码（AWG、ctxbridges、GL、SDL 适配等）★红线
│       │   ├── jniLibs/              # 预编译 .so ★红线
│       │   ├── assets/               # game / controllers / legal / options.txt / app_runtime 等
│       │   ├── jreAssets/            # 内置 OpenJDK 运行时（按 ABI 打包）
│       │   └── res/                  # 资源、文案、图标、主题
│       ├── test/                     # 单元测试
│       └── androidTest/              # 仪器测试（VersionSetting、FCLGameRepository 等）
├── LWJGL/                            # LWJGL 3.3.3 / 3.4.1 模块（官方 jar + Android 补丁）★红线
├── Terracotta/                       # 联机组网模块（JNI 封装）★红线
├── ZipFileSystem/                    # OpenJDK zipfs 移植 ★红线
├── gradle/libs.versions.toml         # 版本目录（compileSdk=35、AGP=8.13.2、Kotlin=2.4.10）
├── gradle/wrapper/gradle-wrapper.properties  # Gradle 8.14.4（本项目本地已指向腾讯镜像）
├── docs/source-reading/              # 上游官方源码通读报告（12 篇，架构参考首选）
└── scripts/                          # 构建/发布辅助脚本
```

### 构建入口与产物

- 入口：根目录 `gradlew.bat`（Windows）/ `gradlew`，唯一模块 `:FCL`。
- 关键启动链路：
  `LauncherHelper.launch()` → `FCLGameRepository.getLaunchOptions()` → `FCLGameLauncher.launch()` →
  `DefaultLauncher.generateCommandLine()`（参数装配） → `FCLauncher.launchMinecraft()`（JNI 桥） →
  `JVMActivity` → `FCLBridge.execute()` → 原生侧拉起 OpenJDK。
- 产物：`FCL/build/outputs/apk/<buildType>/`。
- 签名：`debug`/`release` 使用 `key-store.jks`（密钥 `FCLKey`，密码来自环境变量 `FCL_KEYSTORE_PASSWORD` 或 `local.properties` 的 `pwd`）；`fordebug` 使用仓库内 `debug-key.jks`（密码固定 `FCL-Debug`），是上游 CI 给 fork 使用的构建类型。

## 3. 构建与验证（本机实测）

本机环境（2026-11-11 验证）：

- JDK：`C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot`（上游 CI 用 JDK 17；Gradle 8.14.4 + AGP 8.13.2 在 JDK 21 下可用）
- Android SDK：`D:\AI\thirdparty\android-sdk`
  - `cmdline-tools;latest`（sdkmanager 19.0）
  - `platform-tools`、`platforms;android-35`、`build-tools;35.0.0`、`cmake;3.22.1`
  - `ndk;27.0.12077973`（对应 NDK r27）
- Git：`D:\AI\thirdparty\Git\cmd\git.exe`（用户级 PATH 已包含）
- 仓库根 `local.properties`（本地文件，不入库）：
  ```properties
  sdk.dir=D\:\\AI\\thirdparty\\android-sdk
  arch=arm64
  ```
- 本地镜像配置（属构建环境，不属业务代码）：
  - Gradle 分发包：`gradle/wrapper/gradle-wrapper.properties` 指向 `https://mirrors.cloud.tencent.com/gradle/gradle-8.14.4-bin.zip`
  - Maven 依赖：`C:\Users\<user>\.gradle\init.d\aliyun-mirror.gradle.kts`（阿里云 google / public / gradle-plugin，仓库内 `repositories` 声明未改）

构建命令（Windows PowerShell）：

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:ANDROID_HOME="D:\AI\thirdparty\android-sdk"
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="D:\AI\thirdparty\Git\cmd;" + $env:Path
.\gradlew.bat assembleFordebug -Darch=arm64 --max-workers=2 --no-daemon
```

说明：
- `-Darch=arm64` 只打包 arm64-v8a 的 JRE/原生资源；不传则全 ABI，体积与耗时显著增加。
- 若 `assembleDebug` 因 `FCLKey` 签名密码缺失失败，等价的可构建目标是 `assembleFordebug`（上游给 fork 的 CI 路径，使用 `debug-key.jks`）。
- 验证：`FCL/build/outputs/apk/fordebug/` 下的 APK 可正常安装启动；首次构建需联网拉 Gradle 与依赖。
- 仓库与 SDK 必须位于纯 ASCII 路径（当前 `D:\AI\FCLShell`、`D:\AI\thirdparty`）。路径含中文时 native 工具链会失败：`clang: error: unable to execute command: unspecified system_category error`。
- 长构建不要用后台任务或 `Start-Process`（子进程会被回收、日志 0 字节），用前台阻塞执行并落盘日志；本机内存紧张时加 `--max-workers=2` 与 `-Dorg.gradle.jvmargs=-Xmx2560m -XX:ReservedCodeCacheSize=256m`，否则 Gradle daemon 可能原生 OOM 崩溃。
- 首次实测产物：`FCL/build/outputs/apk/fordebug/FCL-fordebug-1.3.4.5-arm64-v8a.apk`，166.92 MB。

## 4. 红线（禁止修改）

以下内容一律不得改动，任何需求都必须绕开或在其他层解决：

1. **JVM 启动核心与 JNI 桥**
   - `FCL/src/main/java/com/tungsten/fclauncher/**`（`FCLauncher`、`bridge/FCLBridge`、`VMLauncher`、`SDL` 适配等）
   - `FCL/src/main/java/org/libsdl/**`
2. **原生库与原生代码**
   - `FCL/src/main/jni/**`
   - `FCL/src/main/jniLibs/**`
   - 仓库内任何 `.so` 文件（含 `FCL/src/main/assets/**` 下的渲染器/驱动库）
3. **渲染器**
   - `FCL/src/main/java/com/tungsten/fcl/core/render/RendererManager` 及 `assets` 中 GL4ES / VirGL / Zink / MG / NG-GL4ES 相关库与配置
   - `FCL/src/main/assets/game/**` 中的渲染器配置模板
4. **第三方后端模块**
   - `LWJGL/**`、`Terracotta/**`、`ZipFileSystem/**`

允许改动的层：`com.tungsten.fcl.ui`、`com.tungsten.fcl.setting`、`com.tungsten.fcl.game`（业务编排）、`com.tungsten.fclcore` 中的参数装配/版本模型（尽量少改）、`res/` 资源与 `assets` 中的非原生资源。

## 5. GPL-3.0 合规要点

- 保留仓库根 `LICENSE`（GPL-3.0）与所有源文件头部的版权声明，不得删除或替换为自有版权。
- 修改上游文件时：在文件头或提交信息中说明“修改自 FCL-Team/FoldCraftLauncher”，并保留原版权行。
- 分发 APK 时须同时提供对应源代码（含本项目修改），并保留 GPL-3.0 许可文本与“无担保”声明。
- 新增文件建议使用 GPL-3.0 兼容许可；引入第三方库前先核对许可证兼容性。
- 不得移除 `FCL/src/main/assets/legal/**` 中的开源许可与致谢内容。

## 6. 后续改动约定

- 新功能优先放在**新目录/新包**（例如 `com.tungsten.fcl.shell`），通过 1~2 行挂接调用上游代码，减少与核心耦合，便于跟随上游 `main` 更新。
- 避免直接改 `fclcore` 的参数装配算法；如需定制启动参数，优先在 `LaunchOptions` 构建前注入（见 `outputs/launch-args-server-injection-plan.md`）。
- 与上游同步：保持 `upstream` remote 指向 `https://github.com/FCL-Team/FoldCraftLauncher.git`，定期 `git fetch upstream && git merge upstream/main`，冲突优先保留上游实现。
- 改动前先读 `docs/source-reading/` 对应篇章，确认调用链与线程模型。
- 任何触及第 4 节红线的需求，必须改为“上层封装/配置注入/资源替换”，不得直接修改。
