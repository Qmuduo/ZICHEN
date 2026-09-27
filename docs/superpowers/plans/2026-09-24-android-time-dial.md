# Android 时辰盘与时辰笺实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** 从空仓库建立 Android 应用，交付离线可用的宋式时辰盘、时辰预览和时辰笺。

**产品验收口径：** [Android 首版验收清单](../specs/2026-09-24-ancient-time-companion-v1-acceptance.md)。

**Architecture:** 单 Activity 承载 Compose 页面。独立 Kotlin 时间规则层负责时辰、九十六刻、初正和首版简化更点；本地内容仓库提供诗句与介绍。界面只消费时间状态和内容模型。

**Tech Stack:** Kotlin、Jetpack Compose、AndroidX ViewModel、Android assets JSON。

## Global Constraints

- 首版只支持 Android，离线可用。
- 最低支持 Android 8.0（API 26）；精确构建工具和依赖版本在开工时按稳定版记录。
- 首页采用温纸白、淡墨、低饱和青绿、细刻线和充足留白。
- 时辰沿用需求草案中的现代时间区间，以设备当前民用时间判定，不作经度修正。
- 每个两小时的时辰显示八刻，每刻十五分钟；初、正各覆盖一小时。
- 首版更点固定为 19:00–05:00，每更五点，每点二十四分钟，并标注为“简化对照”。
- 日晷精确计算、AR、完整年/月干支、季节性更点及宜忌不在本计划范围内。

---

## 文件职责

- app/src/main/java/com/qmuduo/zichen/time/TraditionalTimeCalculator.kt：纯函数，输入时间与时区，输出时辰、刻、初正、更点状态。
- app/src/main/java/com/qmuduo/zichen/time/TimeModels.kt：时辰与读时结果的数据模型。
- app/src/main/java/com/qmuduo/zichen/content/TimeContentRepository.kt：读取离线时辰介绍与诗句。
- app/src/main/assets/time_content.json：十二时辰的别称、地支、生肖、生活意象和带出处的诗句。
- app/src/main/java/com/qmuduo/zichen/ui/home/HomeScreen.kt：主表盘和选中时辰预览。
- app/src/main/java/com/qmuduo/zichen/ui/detail/TimeDetailScreen.kt：简短时辰笺。
- app/src/main/java/com/qmuduo/zichen/ui/theme/：纸色、墨色、青绿、字体和间距。
- app/src/main/java/com/qmuduo/zichen/MainActivity.kt：应用入口和页面导航。
- app/src/main/java/com/qmuduo/zichen/time/TimeTicker.kt：在前台按需刷新当前读时，并响应系统时间或时区变更。

## Tasks

### Task 1: 锁定 Android 平台基线

**Files:** 创建 `docs/superpowers/decisions/android-platform-baseline.md`。

**实施：** 采用已确认的 minSdk 26；开始实现时查阅 Android 官方发布信息，记录 compileSdk、targetSdk、Android Gradle Plugin、Kotlin 与 Compose 版本，并说明选择依据。其余版本号以实际开工时的稳定版为准。

**验收：** 决策记录包含 minSdk 26、其覆盖范围、其余版本及官方来源链接；后续 Gradle 骨架按此记录配置。

### Task 2: 建立 Android 应用骨架

**Files:** 创建 Gradle Kotlin DSL 项目、AndroidManifest.xml、MainActivity.kt 和主题文件。

**实施：** 按平台基线创建单 Activity Compose 应用；集中管理依赖版本；确认应用可安装启动。

**验收：** Android 模拟器能启动空白首页；应用不申请网络或位置权限。

### Task 3: 实现独立时间规则

**Files:** 创建 TimeModels.kt、TraditionalTimeCalculator.kt。

**接口：** readAt(instant: Instant, zone: ZoneId): TimeReading 返回当前时辰、刻序号、初/正、可选的更序号和点序号。

**规则：** 刻序号范围为 0–7；初/正按时辰前后各一小时划分；19:00–05:00 分五更、每更五点；午夜跨日时正确处理子时和三更。

**验收：** 边界值覆盖 19:00、21:00、23:00、00:00、01:00、03:00、05:00，以及每个时辰的十五分钟刻界。

### Task 4: 准备离线时辰内容

**Files:** 创建 time_content.json、TimeContentRepository.kt。

**实施：** 为十二时辰录入名称、别称、地支、生肖、简短生活意象和一首带作者/出处的诗句；解析错误时显示可恢复的错误状态。

**验收：** 断网时可读取全部内容；每个时辰字段齐全；首页与详情共用同一内容来源。

### Task 5: 实现首页表盘与预览

**Files:** 创建 HomeScreen.kt 和表盘绘制组件；在 MainActivity.kt 接入导航。

**实施：** 绘制十二个主分区与每区八段浅刻度；用独立视觉状态区分真实当前时辰和用户选中预览；由 TimeTicker 在前台刷新当前读时，并在系统时间或时区变化后重新计算；点选扇区更新预览，点击“时辰笺”进入详情。19:00–05:00 在时辰范围下方以小字显示跟随真实时间的更点，并标注“简化对照”；主页同时提供通往日期详情和计时器的轻量入口。

**验收：** 小屏手机上十二分区可辨认；跨刻、跨时辰和时区变化后读时正确刷新；预览不改变真实当前标记，也不改变“此刻”更点；更点信息只在 19:00–05:00 显示且保持次要层级；用户可以返回实时状态；日期详情和计时器可从主页打开；首页不堆叠历法信息。

### Task 6: 实现时辰笺

**Files:** 创建 TimeDetailScreen.kt。

**实施：** 按时辰名与别称、时间范围与生活意象、诗句及出处、地支生肖补充的顺序呈现。

**验收：** 十二个时辰均可打开；长内容可滚动；诗句出处可见；不显示每日宜忌。
