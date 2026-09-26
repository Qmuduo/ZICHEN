# 钟历独立页 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `subagent-driven-development` (recommended) or `executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 Android 应用中新增独立的自适应钟历页，复用实时十二时辰盘和现有农历月历数据。

**Architecture:** 在现有 `AppPage` 路由中增加钟历页；从 `DialCanvas` 抽出共用时辰盘绘制，让首页保留原交互、钟历页使用纯展示版本。新页面复用 `LunarCalendarProvider`，并通过 `MainActivity` 与原月历共享月份和选中日期状态。

**Tech Stack:** Kotlin、Jetpack Compose Foundation、Compose Canvas、`java.time`、现有 `TraditionalTime` 与 `LunarCalendarProvider`。

## Global Constraints

- 目标平台： Android
- 首页现有「日期」入口保持不变，仍进入农历月历。
- 点击钟历页中的日期只移动选中高亮，不显示日期简介、不打开详情，也不改变标题中的今日农历日期。
- 页面不增加新的历法算法或位置权限。
- 在钟历页增加逐日农历标签、日期简介、宜忌详情或时辰详情不在本次范围内。

---

## File Map

- Create `app/src/main/java/com/qmuduo/zichen/ui/home/ShichenDial.kt`: shared Canvas renderer and passive dial composable.
- Modify `app/src/main/java/com/qmuduo/zichen/ui/home/DialCanvas.kt`: call the shared renderer while retaining homepage readout, poem, and bottom entries.
- Create `app/src/main/java/com/qmuduo/zichen/ui/date/CalendarModeSwitcher.kt`: compact, accessible month/clock-calendar switcher.
- Modify `app/src/main/java/com/qmuduo/zichen/ui/date/LunarCalendarScreen.kt`: add the switcher callback without changing the current calendar’s date summary and detail behavior.
- Create `app/src/main/java/com/qmuduo/zichen/ui/date/ClockCalendarScreen.kt`: adaptive clock-calendar layout and date-only month grid.
- Modify `app/src/main/java/com/qmuduo/zichen/MainActivity.kt`: route, shared calendar state, page transitions, and current-date refresh while either calendar page is visible.
- Do not change `LunarCalendarProvider`, `LocalLunarCalendarProvider`, lunar conversion dependencies, or app permissions.

## Task 1: Extract and reuse the twelve-shichen dial

**Files:**
- Create: `app/src/main/java/com/qmuduo/zichen/ui/home/ShichenDial.kt`
- Modify: `app/src/main/java/com/qmuduo/zichen/ui/home/DialCanvas.kt`

**Interfaces:**
- `ShichenDial` consumes a size modifier and the current `LocalTime`; it renders the existing dial face, twelve branch labels, 96刻度, and 初/正刻 markers without click/preview interactions. The center remains limited to the gnomon and hub; time and shichen text stay outside the face.
- The shared drawing entry point is:

```kotlin
internal fun drawShichenDial(
    canvas: android.graphics.Canvas,
    centerX: Float,
    centerY: Float,
    unit: Float,
    actualTime: LocalTime,
    currentIndex: Int,
    previewIndex: Int? = null,
)
```

- The reusable Compose wrapper is:

```kotlin
@Composable
internal fun ShichenDial(
    modifier: Modifier,
    actualTime: LocalTime,
)
```

- `DialCanvas` remains responsible for the homepage heading, outer readout, poem, and bottom actions. It calls `drawShichenDial` with its existing `currentIndex`, `actualTime`, and `previewIndex` so homepage preview behavior remains intact.

- [ ] Move the dial-only drawing helpers (`drawPreviewSector`, tick/刻 labels, branches, stems, gnomon, current arc, and circle-point math) into `ShichenDial.kt`.
- [ ] Add `drawShichenDial` and the passive `ShichenDial` Canvas wrapper; scale its dial from the smaller canvas dimension so it can render in both page layouts.
- [ ] Replace the corresponding drawing calls in `DialCanvas` with one call to `drawShichenDial`; leave homepage text and gesture handling in their current owners.
- [ ] Confirm the common renderer leaves the dial center empty except for the gnomon and hub; keep time and shichen text outside the face.
- [ ] Build with `.\gradlew.bat :app:assembleDebug` and inspect the existing homepage to confirm the shared renderer preserves its preview highlight and live gold indicator.
- [ ] Commit as `refactor: extract reusable shichen dial`.

## Task 2: Add a shared month/clock-calendar switcher

**Files:**
- Create: `app/src/main/java/com/qmuduo/zichen/ui/date/CalendarModeSwitcher.kt`

**Interfaces:**

```kotlin
@Composable
internal fun CalendarModeSwitcher(
    isClockCalendar: Boolean,
    onSelectMonth: () -> Unit,
    onSelectClockCalendar: () -> Unit,
)
```

- [ ] Create the switcher using existing `HomePalette` colors and the project’s Foundation text/clickable patterns; expose both options as accessible buttons.
- [ ] Keep each option’s minimum touch target at 48dp and verify the compact switcher fits in a 360dp-wide portrait layout.
- [ ] Run `.\gradlew.bat :app:assembleDebug` and confirm the existing calendar screen still compiles.
- [ ] Commit as `feat: add calendar mode switcher`.

## Task 3: Build the adaptive clock-calendar screen

**Files:**
- Create: `app/src/main/java/com/qmuduo/zichen/ui/date/ClockCalendarScreen.kt`
- Use: `ShichenDial.kt`, `CalendarModeSwitcher.kt`, `LunarCalendarProvider.kt`, and existing `HomePalette`.

**Interface:**

```kotlin
@Composable
internal fun ClockCalendarScreen(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    provider: LunarCalendarProvider,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToday: () -> Unit,
    onOpenMonthCalendar: () -> Unit,
)
```

- The screen owns a one-second local-time ticker and passes the time to `ShichenDial`. It obtains the six-week grid from `provider.getMonth(month)` and the header’s today-only lunar text from `provider.getDate(today)`.
- A private `ClockCalendarDayCell(day: LunarCalendarDay?, selectedDate: LocalDate, onSelectDate: (LocalDate) -> Unit)` renders only the Gregorian day number and selected state. Null cells remain empty padding cells.

- [ ] Create the screen root with status/navigation insets and dark `HomePalette` background.
- [ ] Use `BoxWithConstraints` and choose the layout with `maxWidth > maxHeight`: landscape places the passive dial and its shichen/time readout in the left pane and the full month grid in the right pane; portrait puts a 112dp compact dial at the top and the full month grid below it.
- [ ] Put shichen and time readout outside the dial face. Keep the center limited to the existing gnomon and hub.
- [ ] Add the shared mode switcher with the clock-calendar option selected; retain month navigation and a today action.
- [ ] Render weekdays and all six weeks from `LunarCalendarMonth.cells`; display Gregorian numbers only, with a restrained selected-date highlight and no date summary or detail action.
- [ ] Show the Gregorian month and today’s lunar date in the month heading. If lunar data is unavailable, show `农历信息暂不可用` while keeping the dial and Gregorian grid visible.
- [ ] Give each date cell an accessibility description with its Gregorian date and selected state; do not expose missing per-cell lunar content.
- [ ] Run `.\gradlew.bat :app:assembleDebug` and confirm the standalone screen compiles before it is connected to navigation.
- [ ] Commit as `feat: add adaptive clock calendar screen`.

## Task 4: Wire navigation and shared calendar state

**Files:**
- Modify: `app/src/main/java/com/qmuduo/zichen/MainActivity.kt`
- Modify: `app/src/main/java/com/qmuduo/zichen/ui/date/LunarCalendarScreen.kt`
- Use: `app/src/main/java/com/qmuduo/zichen/ui/date/ClockCalendarScreen.kt`

- [ ] Add `CLOCK_CALENDAR` to `AppPage` and render `ClockCalendarScreen` from the `when` expression.
- [ ] Add `onOpenClockCalendar: () -> Unit` to `LunarCalendarScreen`; place `CalendarModeSwitcher` in its title row with month selected, preserving back, today, date summary, and details controls.
- [ ] Route the month-screen switcher to `CLOCK_CALENDAR`, and route the clock-screen switcher back to `DATE_DETAIL`. Leave `HomeScreen.onOpenDate` targeting `DATE_DETAIL`.
- [ ] Pass the existing `calendarMonth`, `selectedDate`, `today`, and `calendarProvider` to both pages; use the same month-change, date-select, and today callbacks so both displays stay synchronized.
- [ ] Extend the existing once-per-minute current-date refresh to run while `CLOCK_CALENDAR` is visible.
- [ ] Extend `BackHandler` so Android Back from `CLOCK_CALENDAR` returns to `DATE_DETAIL`, from almanac detail returns to `DATE_DETAIL`, and other secondary screens return to `HOME`.
- [ ] Confirm selecting a date on the clock page changes only the shared selected date and highlight; it does not navigate to almanac detail.
- [ ] Run `.\gradlew.bat :app:assembleDebug` and confirm all page callbacks and route cases compile.
- [ ] Commit as `feat: connect clock calendar navigation`.

## Task 5: Build and manually verify both layouts

**Files:**
- No source files; verification only.

- [ ] Run `.\gradlew.bat :app:assembleDebug` and confirm the debug APK is produced.
- [ ] Run `adb install -r .\app\build\outputs\apk\debug\app-debug.apk` on the connected Android device, then open the app. If no device is connected, install the same APK on an Android emulator.
- [ ] Enter the existing month calendar from the home date action; confirm it remains the default destination and its summary/detail flow is unchanged.
- [ ] Switch to Clock Calendar; confirm the month, selected date, today action, and month navigation match the month page.
- [ ] Rotate between portrait and landscape; confirm the dial moves from the compact top ornament to the left pane and the month grid remains complete.
- [ ] Wait for a time tick and a shichen boundary; confirm the gnomon and current shichen update while tapping the dial does nothing.
- [ ] Select another date; confirm only the highlight moves and the top lunar caption remains today’s date. Use today and Android Back to confirm their specified behavior.
- [ ] Commit any final layout correction as `fix: refine clock calendar layout`.

## Verification Notes

- This plan adds no new automated test source. Verification follows the approved design’s Debug build and manual device/emulator checks.
- Existing `TraditionalTimeTest`, `DialGeometryTest`, and `HomeDialStateTest` remain unchanged; this plan does not add new time or calendar algorithms.
