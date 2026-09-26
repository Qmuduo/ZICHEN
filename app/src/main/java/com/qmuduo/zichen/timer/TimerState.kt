package com.qmuduo.zichen.timer

enum class TimerPreset(
    val title: String,
    val durationMillis: Long,
) {
    ONE_QUARTER("一刻", 15 * 60_000L),
    INCENSE("一炷香", 30 * 60_000L),
    HALF_SHICHEN("半个时辰", 60 * 60_000L),
}

enum class TimerStatus {
    READY,
    RUNNING,
    PAUSED,
    FINISHED,
}

data class TimerState(
    val preset: TimerPreset = TimerPreset.ONE_QUARTER,
    val status: TimerStatus = TimerStatus.READY,
    val deadlineEpochMillis: Long? = null,
    val remainingMillis: Long = preset.durationMillis,
    val soundEnabled: Boolean = false,
    val reminderExplainerSeen: Boolean = false,
    val completionNotificationPosted: Boolean = false,
) {
    fun remainingAt(nowEpochMillis: Long): Long = when (status) {
        TimerStatus.RUNNING -> (deadlineEpochMillis ?: nowEpochMillis).minus(nowEpochMillis).coerceAtLeast(0L)
        TimerStatus.PAUSED -> remainingMillis.coerceAtLeast(0L)
        TimerStatus.READY -> preset.durationMillis
        TimerStatus.FINISHED -> 0L
    }

    fun start(nowEpochMillis: Long): TimerState = copy(
        status = TimerStatus.RUNNING,
        deadlineEpochMillis = nowEpochMillis + preset.durationMillis,
        remainingMillis = preset.durationMillis,
        completionNotificationPosted = false,
        reminderExplainerSeen = true,
    )

    fun pause(nowEpochMillis: Long): TimerState {
        if (status != TimerStatus.RUNNING) return this
        val remaining = remainingAt(nowEpochMillis)
        return copy(
            status = if (remaining == 0L) TimerStatus.FINISHED else TimerStatus.PAUSED,
            deadlineEpochMillis = null,
            remainingMillis = remaining,
        )
    }

    fun resume(nowEpochMillis: Long): TimerState {
        if (status != TimerStatus.PAUSED) return this
        if (remainingMillis <= 0L) return copy(status = TimerStatus.FINISHED, deadlineEpochMillis = null)
        return copy(
            status = TimerStatus.RUNNING,
            deadlineEpochMillis = nowEpochMillis + remainingMillis,
        )
    }

    fun refresh(nowEpochMillis: Long): TimerState {
        if (status != TimerStatus.RUNNING || remainingAt(nowEpochMillis) > 0L) return this
        return copy(
            status = TimerStatus.FINISHED,
            deadlineEpochMillis = null,
            remainingMillis = 0L,
        )
    }

    fun reset(): TimerState = copy(
        status = TimerStatus.READY,
        deadlineEpochMillis = null,
        remainingMillis = preset.durationMillis,
        completionNotificationPosted = false,
    )

    fun selectPreset(newPreset: TimerPreset): TimerState {
        if (status != TimerStatus.READY && status != TimerStatus.FINISHED) return this
        return copy(
            preset = newPreset,
            status = TimerStatus.READY,
            deadlineEpochMillis = null,
            remainingMillis = newPreset.durationMillis,
            completionNotificationPosted = false,
        )
    }
}
