# Android 首页视觉实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 从空仓库建立可运行的 Android Compose 工程，实现符合 2026-09-25 最新视觉稿的时辰首页，并验证实时/预览交互及两种小屏比例。（完成）

**Architecture:** 单 Activity + Compose Canvas。纯 Kotlin 时辰规则和表盘几何分别独立，首页只组合实时读数、预览选择与绘制；圆盘绘制仅包含刻度、环形文字、预览扇区、实时金色标记与晷针，时辰读数和诗句绘制在盘外。

**Tech Stack:** Kotlin 2.3.21、Jetpack Compose、AGP 8.13.2、Gradle 8.13、compileSdk/targetSdk 36、minSdk 26。版本依据见 `docs/superpowers/decisions/android-platform-baseline.md`。

## Global Constraints

- 最新视觉源：`outputs/home-2026-09-25-object/home.html`、`small-screen.html`、`realtime@2x.png`、`preview@2x.png`、`small-screen.png`、`two-states.png`。
- 首页背景为 `#0F0E0D`；金色实时标记为 `#D9A94B`；预览高亮为 `#7FBFA4`；使用克制的宋体/衬线字形和留白。
- 圆盘中心只画晷针、晷针投影与轴心；不在中心放时辰名、读数或诗句。
- 十二时辰、二十四个“初/正”标记、十天干环字及九十六刻线围绕中心排列。
- 用户预览时，绿色高亮跟随所选时辰，金色弧线始终跟随真实当前时辰。
- 预览读数将真实当前的分钟数映射到所选时辰起始小时；诗句、读数和现代时间区间置于盘外。
- 保留“回到此刻”、日期、计时视觉入口；本次只实现首页，不构建详情/计时页面。
- 不申请网络、位置或通知权限；日晷计算、AR 与全历法不属于本次工作。

---

## 文件结构

- `settings.gradle.kts`、`build.gradle.kts`、`gradle/libs.versions.toml`、`app/build.gradle.kts`：构建配置与集中版本目录。
- `app/src/main/AndroidManifest.xml`：无敏感权限的应用入口配置。
- `app/src/main/java/com/qmuduo/zichen/MainActivity.kt`：全屏边到边的单 Activity 入口。
- `app/src/main/java/com/qmuduo/zichen/time/TraditionalTime.kt`：时辰、初正、刻、现代时间范围与简化更点的纯规则。
- `app/src/main/java/com/qmuduo/zichen/ui/home/HomeDialState.kt`：将真实当前索引与用户预览索引分离。
- `app/src/main/java/com/qmuduo/zichen/ui/home/DialGeometry.kt`：扇区命中和随本地时间连续变化的晷针角度计算。
- `app/src/main/java/com/qmuduo/zichen/ui/home/HomeScreen.kt`：Compose 屏幕状态、真实时钟刷新、预览与返回此刻状态。
- `app/src/main/java/com/qmuduo/zichen/ui/home/DialCanvas.kt`：表盘和盘外读数的 Canvas 绘制。
- `app/src/main/java/com/qmuduo/zichen/ui/theme/HomePalette.kt`：视觉稿颜色常量。
- `app/src/test/java/com/qmuduo/zichen/time/TraditionalTimeTest.kt`：跨午夜、刻边界、初正与更点规则。
- `app/src/test/java/com/qmuduo/zichen/ui/home/HomeDialStateTest.kt`：预览改变盘外展示，但不改变真实当前索引。
- `app/src/test/java/com/qmuduo/zichen/ui/home/DialGeometryTest.kt`：十二扇区命中、中心/圆盘外保护、晷针跨时辰角度和实时/预览分离。

## Task 1：建立可构建的 Android Compose 骨架（完成）

**接口：** `MainActivity` 调用 `setContent { HomeScreen() }`；只使用本地 Compose UI 依赖。

- [x] 创建 version catalog、Gradle 项目文件、Manifest 和 `MainActivity.kt`，按平台基线锁定版本。
- [x] 创建基础主题色文件，不加入无关依赖或权限。
- [x] 生成 Gradle Wrapper 8.13 并运行 `./gradlew :app:assembleDebug`。
- [x] 确认工程能构建 API 36 APK；模拟器运行留待最终 UI 完成。

## Task 2：先测试再实现时辰规则（完成）

**接口：** `TraditionalTime.readAt(LocalTime): TimeReading`；`TraditionalTime.watchAt(LocalTime): WatchReading?`。

- [x] 编写以下行为测试（用 JUnit4 和手写字面预期）：

```kotlin
@Test fun midnightBelongsToZiAndKeepsLocalClockText() {
    val reading = TraditionalTime.readAt(LocalTime.of(0, 18))
    assertEquals("子", reading.branch)
    assertEquals("夜半", reading.alias)
    assertEquals("正二刻", reading.quarterLabel)
    assertEquals("23:00 — 01:00", reading.interval)
}

@Test fun shichenBoundaryMovesAtOddHours() {
    assertEquals("丑", TraditionalTime.readAt(LocalDateTime.of(2026, 9, 26, 1, 0)).branch)
    assertEquals("寅", TraditionalTime.readAt(LocalDateTime.of(2026, 9, 26, 3, 0)).branch)
}

@Test fun nightWatchUsesFiveTwentyFourMinutePointsAndStopsAtFive() {
    assertEquals("三更四点", TraditionalTime.watchAt(LocalTime.of(0, 18))?.label)
    assertEquals(null, TraditionalTime.watchAt(LocalTime.of(5, 0)))
}
```

- [x] 运行 `./gradlew :app:testDebugUnitTest`，确认因 `TraditionalTime` 尚不存在而失败。
- [x] 实现十二时辰别称、开始小时、诗句、时辰读数、初/正和简化五更点。
- [x] 再运行 `./gradlew :app:testDebugUnitTest`，确认测试通过；补齐 `23:00`、`05:00` 和刻边界样例。

## Task 3：先测试再实现表盘选择几何（完成）

**接口：** `DialGeometry.sectorAt(x, y, centerX, centerY, radius): Int?` 与 `pointerRotationDegrees(LocalTime)`；子时在正上方并按顺时针编号。

- [x] 为顶部子时、右上丑时、底部午时、地支中心、中心留白和圆外点击写 JUnit4 行为测试。
- [x] 运行 `./gradlew :app:testDebugUnitTest`，确认失败原因是 `DialGeometry` 尚不存在。
- [x] 实现角度归一化和半径判定；让 30° 扇区以地支视觉中心为中心，按顺时针映射，并将命中区域限制在表盘环带。
- [x] 再运行 `./gradlew :app:testDebugUnitTest`，确认通过。

**预览状态接口：** `HomeDialState.select(index)`、`renderFor(currentIndex)`、`returnToCurrent()`。

- [x] 测试当前时辰为子时、选择巳时后 `actualIndex` 仍为 `0` 且 `displayIndex` 为 `5`；返回此刻后两个索引都为 `0`。
- [x] 实现预览状态纯数据类，并在首页绘制实时金弧时始终读取 `actualIndex`。
- [x] 再运行 `./gradlew :app:testDebugUnitTest`，确认通过。

## Task 4：绘制首页与预览状态（完成）

- [x] 实现适配 `393×852` 与 `360×800` 的布局比例：顶部标题、主表盘、盘外读数/诗句/时间、更点提示及底部轻入口。
- [x] 在 Canvas 中绘制 96 条刻线、十二地支、二十四个初/正标记、十天干、晷针投影与轴心；中心留白。
- [x] 绘制预览青绿扇区和真实当前时辰的金色外弧，确保改变预览时金色弧线不变；晷针保持金色并实时指向当前时间。
- [x] 首页每秒刷新系统本地时间；晷针每两小时推进 30°，在 24 小时内连续旋转一周；点击扇区只改变预览；预览读数使用所选时辰起始小时加真实分钟；“回到此刻”清除预览。
- [x] 为表盘提供整体读屏描述及十二时辰无障碍自定义操作。

## Task 5：构建、运行并对照视觉稿（完成）

- [x] 运行 `./gradlew :app:testDebugUnitTest :app:assembleDebug`；测试与 APK 构建成功。
- [x] 使用 API 36 模拟器设置 `393×852 dp` 和 `360×800 dp`，采集实时与预览截图；随后按用户明确要求将 Debug APK 安装到 Samsung SM_F936N 实体手机。
- [x] 将实时、预览、小屏截图与参考 PNG 对照；核对暗底、表盘位置/大小、刻线密度、中心留白、盘外读数层级和预览时保留的当前金弧。
- [x] 修正入口接线、预览晷针颜色、扇区触摸偏移及晷针静态角度，并重新构建、安装和启动；实机 `12:09` 时指针指向午时。最终 APK：`app/build/outputs/apk/debug/app-debug.apk`；模拟器截图：`outputs/android-home-review/`。Android 状态栏的图标与时间由系统绘制。
