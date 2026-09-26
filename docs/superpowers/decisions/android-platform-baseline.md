# Android 平台基线

**决定日期：** 2026-09-26  
**首版最低版本：** Android 8.0（API 26）

## 选择

- `minSdk = 26`：沿用已确认的产品草案。
- `compileSdk = 36`、`targetSdk = 36`：使用稳定的 Android 16 API。
- Android Gradle Plugin `8.13.2`，Gradle Wrapper `8.13`，Android Build Tools `36.0.0`。
- Kotlin `2.3.21`，Compose Compiler Gradle Plugin `2.3.21`。
- Compose BOM `2026.06.01`。
- JDK 17 及以上；本机使用 JDK 21。

Android 17 的 API 37 与 Compose 1.12 所需的 API 37 仍属于预览开发目标，因此本次保持在 API 36 稳定线。AGP 8.13 支持 API 36.1 且要求 Gradle 8.13；AGP 8.13.2 的 R8 支持 Kotlin 2.3。Compose Compiler Gradle Plugin 与 Kotlin 版本保持一致。Compose BOM 2026.06.01 是 Maven 仓库中存在的 API 36 稳定 BOM（2026.07.00 不存在）。

本机已安装 Build Tools 34.0.0 与 36.0.0，但没有 AGP 默认请求的 35.0.0；应用构建因此显式使用 36.0.0，避免安装缺失工具时触发远程 SDK 下载。

## 官方依据

- [Android Gradle Plugin 8.13 发布说明](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [AGP 与 Gradle 版本兼容表](https://developer.android.com/build/releases/about-agp)
- [Compose Compiler Gradle Plugin](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [AndroidX Compose 稳定版本记录](https://developer.android.com/jetpack/androidx/versions/stable-channel)
- [Kotlin 发布记录](https://kotlinlang.org/docs/releases.html)
- [Android 17 发布说明](https://developer.android.com/about/versions/17/release-notes)
