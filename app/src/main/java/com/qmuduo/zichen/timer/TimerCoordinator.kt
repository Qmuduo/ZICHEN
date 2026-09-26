package com.qmuduo.zichen.timer

import android.content.Context

class TimerCoordinator(context: Context) {
    private val appContext = context.applicationContext
    private val repository = TimerRepository(appContext)
    private val scheduler = TimerAlarmScheduler(appContext)
    private val notifications = TimerNotifications(appContext)

    init {
        notifications.ensureChannels()
    }

    fun restore(nowEpochMillis: Long = System.currentTimeMillis()): TimerState = synchronized(STATE_LOCK) {
        val stored = repository.load()
        val restored = stored.refresh(nowEpochMillis)
        repository.save(restored)
        if (restored.status == TimerStatus.RUNNING) scheduler.schedule(restored) else scheduler.cancel()
        maybePostCompletion(restored)
    }

    fun start(preset: TimerPreset, nowEpochMillis: Long = System.currentTimeMillis()): TimerState = synchronized(STATE_LOCK) {
        val current = repository.load().selectPreset(preset)
        val started = current.start(nowEpochMillis)
        repository.save(started)
        scheduler.schedule(started)
        notifications.clearCompleted()
        started
    }

    fun pause(nowEpochMillis: Long = System.currentTimeMillis()): TimerState = synchronized(STATE_LOCK) {
        val paused = repository.load().pause(nowEpochMillis)
        repository.save(paused)
        scheduler.cancel()
        maybePostCompletion(paused)
    }

    fun resume(nowEpochMillis: Long = System.currentTimeMillis()): TimerState = synchronized(STATE_LOCK) {
        val resumed = repository.load().resume(nowEpochMillis)
        repository.save(resumed)
        if (resumed.status == TimerStatus.RUNNING) scheduler.schedule(resumed)
        maybePostCompletion(resumed)
    }

    fun reset(): TimerState = synchronized(STATE_LOCK) {
        val reset = repository.load().reset()
        repository.save(reset)
        scheduler.cancel()
        notifications.clearCompleted()
        reset
    }

    fun selectPreset(preset: TimerPreset): TimerState = synchronized(STATE_LOCK) {
        val next = repository.load().selectPreset(preset)
        repository.save(next)
        next
    }

    fun toggleSound(): TimerState = synchronized(STATE_LOCK) {
        val current = repository.load()
        val next = current.copy(soundEnabled = !current.soundEnabled)
        repository.save(next)
        next
    }

    fun refresh(nowEpochMillis: Long = System.currentTimeMillis()): TimerState = synchronized(STATE_LOCK) {
        val stored = repository.load()
        val refreshed = stored.refresh(nowEpochMillis)
        if (refreshed != stored) repository.save(refreshed)
        if (refreshed.status == TimerStatus.FINISHED) scheduler.cancel()
        maybePostCompletion(refreshed)
    }

    fun handleAlarm(nowEpochMillis: Long = System.currentTimeMillis()): TimerState = synchronized(STATE_LOCK) {
        val refreshed = repository.load().refresh(nowEpochMillis)
        repository.save(refreshed)
        if (refreshed.status == TimerStatus.RUNNING) scheduler.schedule(refreshed)
        else scheduler.cancel()
        maybePostCompletion(refreshed)
    }

    fun exactAlarmAllowed(): Boolean = scheduler.canScheduleExactAlarms()

    fun reminderSettingsIntent() = scheduler.exactAlarmSettingsIntent()

    private fun maybePostCompletion(state: TimerState): TimerState {
        if (state.status != TimerStatus.FINISHED || state.completionNotificationPosted) return state
        if (!notifications.postCompleted(state)) return state
        val updated = state.copy(completionNotificationPosted = true)
        repository.save(updated)
        return updated
    }

    private companion object {
        val STATE_LOCK = Any()
    }
}
