# Android 日期详情实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** 为 Android 首版提供离线的今日干支和当前节气详情。

**产品验收口径：** [Android 首版验收清单](../specs/2026-09-24-ancient-time-companion-v1-acceptance.md)。

**Architecture:** 日期规则通过独立 DateInfoProvider 暴露；UI 不直接计算干支或节气。日干支由本地公历日期按六十日循环计算；节气由带来源时区信息的年度时刻表离线查询。

**Tech Stack:** Kotlin、Java Time API、AndroidX ViewModel、离线资源或纯 Kotlin 日历算法。

## Global Constraints

- 首版离线可用，无账号、服务器依赖或位置权限。
- 只展示今日干支和当前节气；完整年柱/月柱、节气倒计时不在范围内。
- 采用设备当前时区和本地日期；日干支在当地 00:00 换日，不采用 23:00 子初换日。
- 节气表至少包含当前年和前一年已发布的年度资料；按来源时区转换为绝对时刻后与当前时刻比较，数据精度为分钟。
- 超出已打包节气资料年份时，显示明确的“资料暂不可用”；仍正常计算并显示今日干支，不以长期公式填补节气时刻。

---

## 文件职责

- app/src/main/java/com/qmuduo/zichen/date/DateInfo.kt：本地日期、日干支和当前节气信息模型。
- app/src/main/java/com/qmuduo/zichen/date/DateInfoProvider.kt：日期信息查询接口。
- app/src/main/java/com/qmuduo/zichen/date/LocalDateInfoProvider.kt：离线实现。
- app/src/main/java/com/qmuduo/zichen/ui/date/DateDetailScreen.kt：日期详情页面。
- docs/superpowers/decisions/date-info-source.md：来源、精度、许可与边界规则的决策记录。

## Tasks

### Task 1: 确定离线历法策略

**实施：** 日干支采用纯 Kotlin 本地日期循环计算；节气采用随 App 发布的本地年度时刻表。以紫金山天文台近期年度资料作为节气首选来源；以其 1900–2025 年历资料中的 1949–2025 日干支核对算法，并以香港天文台 2026 年历交叉核对当前年份。每年更新数据包时补入新年度资料。

**验收：** 决策记录给出单一方案、来源链接、数据覆盖与更新规则、换日规则、分钟级节气精度和超范围降级行为；2026 年 1 月 1 日查询依赖 2025 年冬至数据。

### Task 2: 定义日期信息接口

**Files:** 创建 DateInfo.kt、DateInfoProvider.kt。

**接口：** getDateInfo(instant: Instant, zone: ZoneId): DateInfo 返回对应本地日期、dayGanzhi 与 currentSolarTerm。使用具体时刻是为了让节气交接当天能正确区分交节前后。

**验收：** 模型只包含首版所需字段；调用方无需知道底层使用历表还是算法；查询在节气交接时刻前后返回对应的节气。

### Task 3: 实现本地日期信息提供器

**Files:** 创建 LocalDateInfoProvider.kt 和决策记录指定的本地数据/算法文件。

**实施：** 实现干支日与当前节气查询；将边界样例与选定的校对来源逐一比对；不请求网络或定位。日干支要验证 00:00 前后翻转且 23:00 不翻转；节气要验证 2026-04-05 02:39 仍显示春分、02:40 起显示清明，并验证 2026-01-01 使用前一年冬至数据。

**验收：** 支持范围内的日期断网可查询；日干支在指定校对范围内与年度历表一致；节气交接按表内分钟边界变化；缺少节气数据时只将节气标为不可用，不展示猜测结果，今日干支仍可显示。

### Task 4: 接入日期详情页面

**Files:** 创建 DateDetailScreen.kt；在主页提供轻量日期入口。

**实施：** 以次要信息层级显示本地日期、今日干支、当前节气；页面内容来自 DateInfoProvider。

**验收：** 首页仍只突出时辰盘与诗句；日期页离线可读；更换设备时区后遵循已记录的规则。
