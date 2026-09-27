# 子辰视觉精修实施计划

**Goal:** 将已确认的墨底古铜方向落实到 Android 界面。
**Architecture:** 使用共享色板、背景和按压修饰器；保留现有 Canvas 表盘与 Compose 页面结构。
**Tech Stack:** Kotlin、Compose Foundation、Android Canvas。

## 约束
保留工作区已有未提交功能；不改时间、历法和计时持久化规则，不增加运行时依赖。

## 执行清单
- [x] theme/HomePalette.kt：替换色板，新增 Surface、Rule 和铜色阴影。
- [x] theme/PolishedSurface.kt：共享背景 drawWithCache 与基于 interactionSource 的按压反馈。
- [x] home/ShichenDial.kt、DialCanvas.kt：盘面径向光、细边缘、铜针、精简文字层级和底部入口底面。
- [x] date/*.kt、detail/TimeDetailScreen.kt、timer/TimerScreen.kt：应用背景、细边框与统一选中态，调整数字字体。
- [x] 执行 gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug；在模拟器安装、截图复核并修正问题。
- [x] 保存验证记录和可安装 APK 路径。
