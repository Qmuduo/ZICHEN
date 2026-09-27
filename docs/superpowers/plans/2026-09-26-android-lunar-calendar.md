# Android Lunar Calendar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an offline monthly lunar calendar and selected-day almanac details to the existing Android app.

**Architecture:** Keep calendar conversion behind a Kotlin provider in `calendar/`, using `cn.6tail:lunar:1.7.7` to produce app-owned immutable models. Compose screens render the monthly grid, date summary and scrollable detail; `MainActivity` owns only the selected month/date and page navigation state.

**Tech Stack:** Kotlin, Java Time API, Jetpack Compose Foundation, `cn.6tail:lunar:1.7.7`.

## Global Constraints

- “2025-01-01 至 2028-12-31 为本版完整离线黄历数据保证范围，包含月历所需的农历日期、节气/节日标签和详情字段。”
- “选中日期以设备当前时区的 `LocalDate` 表示；月份、星期、今天高亮均按设备当前时区处理。不使用地理位置修正日期或时间。”
- “月历允许切换覆盖期以外的月份。若离线引擎能提供可靠的农历基础换算，则显示公历日、农历日、星期及可用的基础节气信息；不显示缺少依据的宜忌和神煞。”
- “完整月历，周一为每周第一天；节气或传统节日日期格只增加一条短提示。”
- “详情包含五行/纳音、冲煞、彭祖百忌、喜神/福神/财神方位、宜忌、吉神与凶神，并标注‘传统民俗资料，仅供参考’。”
- “月历不申请网络、账号或位置权限。”
- Preserve the current `HomePalette` and the existing page and timer behavior.
- Do not add or run automated tests for this request; use Gradle compile/build verification only.

---

## File Structure

- `gradle/libs.versions.toml` — pin the lunar library and define its catalog alias.
- `app/build.gradle.kts` — include the library in the Android application.
- `app/src/main/java/com/qmuduo/zichen/calendar/LunarCalendarProvider.kt` — immutable calendar models and the provider interface.
- `app/src/main/java/com/qmuduo/zichen/calendar/LocalLunarCalendarProvider.kt` — adapt the local library to the app models and enforce the 2025–2028 almanac-detail coverage.
- `app/src/main/java/com/qmuduo/zichen/ui/date/LunarCalendarScreen.kt` — month navigation, six-week grid, selected-day summary and return-to-today control.
- `app/src/main/java/com/qmuduo/zichen/ui/date/AlmanacDetailScreen.kt` — screenshot-two field order, folk-calendar notice and unavailable-field behavior.
- `app/src/main/java/com/qmuduo/zichen/MainActivity.kt` — route the existing home date entry to the calendar and preserve month/date when opening and leaving detail.

## Tasks

### Task 1: Add the offline date provider

**Files:**

- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/qmuduo/zichen/calendar/LunarCalendarProvider.kt`
- Create: `app/src/main/java/com/qmuduo/zichen/calendar/LocalLunarCalendarProvider.kt`

**Interfaces:**

- `LunarCalendarProvider.getMonth(month: YearMonth): LunarCalendarMonth`
- `LunarCalendarProvider.getDate(date: LocalDate): LunarCalendarDay`
- `LunarCalendarMonth(month: YearMonth, cells: List<LunarCalendarDay?>)` contains exactly 42 Monday-first cells; `null` represents a blank before or after the Gregorian month.
- `LunarCalendarDay` exposes non-null `date: LocalDate`; nullable `lunarYearName` (lunar-New-Year-based Ganzhi), `zodiac`, `lunarMonthName`, `lunarDayName`, `yearGanzhi` (Li Chun based), `monthGanzhi`, `dayGanzhi`, `solarTerm`, `primaryCellFestival`, `nayin`, `clash`, `shaDirection`, `pengzuGan`, `pengzuZhi`, `xiDirection`, `fuDirection`, `caiDirection`; `weekday`, `festivals`, `yi`, `ji`, `auspiciousDeities`, `inauspiciousDeities`, `hasLunarData`, `hasFullAlmanac`, and `sourceDescription`.

- [ ] Add version `1.7.7` and alias `libs.lunar` for `cn.6tail:lunar`; add `implementation(libs.lunar)` to the app.
- [ ] Define the provider models using `LocalDate`, `YearMonth`, immutable `List` values and nullable event/folk-data fields.
- [ ] Implement `getDate` using `Solar(year, month, day).lunar`; map provider output into app-owned Chinese strings and lists.
- [ ] Use lunar New Year for the lunar year caption and zodiac; use the library's documented Li Chun year pillar and solar-term month pillar, and its day pillar for almanac output.
- [ ] Implement `getMonth` as a Monday-first six-row grid; return blank cells outside the month and day models for every date inside it.
- [ ] Keep all Gregorian/lunar festivals and other observances in the selected-day model, but use only the primary Gregorian/lunar festival lists for the one festival label in a calendar cell; otherwise show that day's solar term. This keeps miscellaneous observance dates from crowding the clean monthly grid.
- [ ] Set `hasFullAlmanac` only for 2025 through 2028 inclusive. Outside that range, retain supported lunar basics but omit Yi/Ji and deity lists; return a safe Gregorian-only model if the library rejects a date.
- [ ] Run `.\gradlew.bat :app:compileDebugKotlin` to resolve the library API and compile the provider.

### Task 2: Build the monthly calendar and date summary

**Files:**

- Create: `app/src/main/java/com/qmuduo/zichen/ui/date/LunarCalendarScreen.kt`
- Reuse: `app/src/main/java/com/qmuduo/zichen/calendar/LunarCalendarProvider.kt`
- Reuse: `app/src/main/java/com/qmuduo/zichen/ui/theme/HomePalette.kt`

**Interfaces:**

- `LunarCalendarScreen(month: YearMonth, selectedDate: LocalDate, today: LocalDate, provider: LunarCalendarProvider, onBack: () -> Unit, onMonthChange: (YearMonth) -> Unit, onSelectDate: (LocalDate) -> Unit, onToday: () -> Unit, onOpenDetails: (LocalDate) -> Unit)`.

- [ ] Render a back/title/today row and a Gregorian month heading with previous/next month controls.
- [ ] Render weekday headings Monday through Sunday and 42 fixed calendar cells; distinguish the selected date, today, event labels and blank cells without relying only on color.
- [ ] In each date cell, show the Gregorian day, lunar day and at most one short festival/solar-term label.
- [ ] Render a compact selected-date summary below the grid with lunar date/year/zodiac, year/month/day Ganzhi, weekday and available event labels.
- [ ] Render the Yi/Ji summary with green and muted red labels when `hasFullAlmanac` is true; otherwise show the coverage notice and leave folk fields out.
- [ ] Make “详情” open the selected date and “回到今天” restore today's month/date; keep the screen vertically scrollable on short devices.
- [ ] Use the existing dark background, serif text, muted dividers and restrained gold from `HomePalette`.

### Task 3: Build the almanac detail screen

**Files:**

- Create: `app/src/main/java/com/qmuduo/zichen/ui/date/AlmanacDetailScreen.kt`
- Reuse: `app/src/main/java/com/qmuduo/zichen/calendar/LunarCalendarProvider.kt`

**Interfaces:**

- `AlmanacDetailScreen(day: LunarCalendarDay, onBack: () -> Unit)`.

- [ ] Render the selected lunar date and year/month/day Ganzhi in the header.
- [ ] Render fields in the approved order: Nayin, clash/sha, Pengzu Gan/Zhi, Xi/Fu/Cai directions, Yi/Ji, auspicious deities and inauspicious deities.
- [ ] Show “传统民俗资料，仅供参考” and the fixed provider/version as the data-method line.
- [ ] For a date outside full almanac coverage, retain available calendar basics and show “该年份未包含完整民俗资料”; omit unavailable Yi/Ji and deity rows.
- [ ] Allow long entries to wrap and scroll; do not clip text on narrow screens.

### Task 4: Connect navigation and preserve selection

**Files:**

- Modify: `app/src/main/java/com/qmuduo/zichen/MainActivity.kt`

- [ ] Replace the home date route with the new lunar calendar screen.
- [ ] Store displayed `YearMonth` and selected `LocalDate` with saveable state, initializing them from the device's local today.
- [ ] When changing month, keep the selected day number where possible and clamp to the last day of a shorter month.
- [ ] Add an almanac-detail route carrying the selected date; back from detail returns to the same month and selected date, while back from calendar returns home.
- [ ] Keep existing time-detail and timer routes unchanged.
- [ ] Run `.\gradlew.bat :app:assembleDebug` and resolve compile/build errors.

## Verification Notes

- Automated tests are not part of this execution because the user asked to begin implementation and did not request tests. Gradle compilation and debug APK assembly are the requested-in-context build checks.
- During implementation, inspect sample output for the 2025–2028 coverage boundary, lunar new year, a leap month, and a date with a solar term; confirm unsupported dates do not expose folk fields.
- This plan does not claim that Yi/Ji and deity rules match every almanac. The app must name the local calculation source and preserve the folk-reference notice.
