# Android 仪式计时器实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** 交付可在后台与锁屏期间继续运行的安静倒计时，并在结束时提醒用户。

**产品验收口径：** [Android 首版验收清单](../specs/2026-09-24-ancient-time-companion-v1-acceptance.md)。

**Architecture:** Compose 页面管理交互；计时状态持久化在本地；Android 闹钟/通知组件负责后台到时提醒；钟磬播放由独立偏好开关控制。

**Tech Stack:** Kotlin、Jetpack Compose、DataStore、Android AlarmManager、NotificationManager、本地音频资源。

## Global Constraints

- 预设为一刻 15 分钟、一炷香 30 分钟、半个时辰 60 分钟。
- 离开 App、锁屏或设备重启时计时继续；完成后发送系统提醒。
- 钟磬音效可选，默认静音。
- 网络不可用时计时完整工作。
- 精确闹钟只用于用户明确启动的计时提醒，并遵循系统权限要求。

---

## 文件职责

- app/src/main/java/com/qmuduo/zichen/timer/TimerState.kt：预设与剩余时间状态。
- app/src/main/java/com/qmuduo/zichen/timer/TimerRepository.kt：本地保存活动计时与用户偏好。
- app/src/main/java/com/qmuduo/zichen/timer/TimerAlarmScheduler.kt：安排、取消和恢复提醒。
- app/src/main/java/com/qmuduo/zichen/timer/TimerFinishedReceiver.kt：处理结束事件并发通知。
- app/src/main/java/com/qmuduo/zichen/timer/TimerBootReceiver.kt：设备重启后读取活动计时，恢复到期状态并重新安排未到期提醒。
- app/src/main/java/com/qmuduo/zichen/timer/TimerNotificationChannels.kt：创建静音与钟磬音提醒渠道，并按用户偏好选择。
- app/src/main/java/com/qmuduo/zichen/ui/timer/TimerScreen.kt：倒计时、沙漏动效和操作控件。
- app/src/main/java/com/qmuduo/zichen/ui/settings/SoundSettings.kt：音效开关与试听入口。
- app/src/main/java/com/qmuduo/zichen/MainActivity.kt：增加计时器页面入口与返回路径。

## Tasks

### Task 1: 固化计时状态模型

**Files:** 创建 TimerState.kt、TimerRepository.kt。

**实施：** 保存预设、绝对到期时刻、暂停剩余时长、运行状态和音效偏好；重新打开 App 或设备重启后从本地状态恢复。重启期间继续计时；如果已过到期时刻，恢复为已完成状态。

**验收：** 开始、暂停、继续和重置后状态一致；进程重建后能恢复剩余时间；活动计时器在重启后恢复剩余时长或已完成状态；预设始终同时显示名称和分钟数。

### Task 2: 接入 Android 后台提醒

**Files:** 创建 TimerAlarmScheduler.kt、TimerFinishedReceiver.kt、TimerBootReceiver.kt；更新 AndroidManifest.xml。

**实施：** 用户首次开始需要后台提醒的倒计时时，先用简短说明解释权限用途，再按 Android 版本请求通知权限，并在需要精确提醒时引导用户授予闹钟与提醒特殊访问权限。用户拒绝精确闹钟权限时仍继续计时，使用非精确的尽力提醒并说明可能延迟；用户拒绝通知权限时不弹系统通知，但保留计时完成状态，重新打开 App 后显示结果。注册系统重启接收器；重启后读取持久化到期时刻，未到期则恢复提醒，已到期则记录完成并在通知权限允许时发出完成提醒。取消或重置时清除旧提醒。参考 [Android 闹钟官方说明](https://developer.android.com/develop/background-work/services/alarms) 与 [Android 通知权限说明](https://developer.android.com/develop/ui/views/notifications/notification-permission)。

**验收：** App 退到后台、锁屏及设备重启后计时继续；重启后未到期时恢复提醒，到期时恢复为完成状态并在通知权限允许时发出提醒；权限可用时结束提醒按精确方案执行；拒绝精确闹钟权限时计时仍完成且说明提醒可能延迟；拒绝通知权限时不会假称已发出通知，重开 App 可看到完成状态；取消或重置不会留下旧提醒。

### Task 3: 实现计时器页面

**Files:** 创建 TimerScreen.kt。

**实施：** 提供三个预设、开始/暂停/继续/重置、剩余时间和缓慢沙漏动画；动画进度从保存的结束时间计算，不依赖页面持续运行；从首页可进入计时器，结束后仍可返回首页。

**验收：** 页面重进后进度正确；锁屏返回后时间不跳回；动画暂停时不影响实际计时。

### Task 4: 实现可选钟磬音效

**Files:** 创建 TimerNotificationChannels.kt、SoundSettings.kt，加入本地短音频资源。

**实施：** 音效默认关闭；用户开启后，结束通知使用带本地钟磬音的通知渠道，由系统随通知播放一次；默认渠道保持静音，避免后台播放受限或通知与播放器重复发声。系统提醒和视觉完成状态始终可用。

**验收：** 静音设置下结束通知无声音；开启后通知播放一次钟磬音；音频资源在飞行模式下可用；用户可在系统设置中继续管理通知声音。
