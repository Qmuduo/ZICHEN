package com.qmuduo.zichen.timer

import android.content.Context
import android.content.SharedPreferences

class TimerRepository(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): TimerState {
        val preset = runCatching {
            TimerPreset.valueOf(preferences.getString(KEY_PRESET, null) ?: return TimerState())
        }.getOrDefault(TimerPreset.ONE_QUARTER)
        val status = runCatching {
            TimerStatus.valueOf(preferences.getString(KEY_STATUS, TimerStatus.READY.name) ?: TimerStatus.READY.name)
        }.getOrDefault(TimerStatus.READY)
        val savedDeadline = preferences.getLong(KEY_DEADLINE, NO_DEADLINE).takeUnless { it == NO_DEADLINE }

        return TimerState(
            preset = preset,
            status = status,
            deadlineEpochMillis = savedDeadline,
            remainingMillis = preferences.getLong(KEY_REMAINING, preset.durationMillis).coerceAtLeast(0L),
            soundEnabled = preferences.getBoolean(KEY_SOUND_ENABLED, false),
            reminderExplainerSeen = preferences.getBoolean(KEY_EXPLAINER_SEEN, false),
            completionNotificationPosted = preferences.getBoolean(KEY_NOTIFICATION_POSTED, false),
        )
    }

    fun save(state: TimerState) {
        preferences.edit()
            .putString(KEY_PRESET, state.preset.name)
            .putString(KEY_STATUS, state.status.name)
            .putLong(KEY_DEADLINE, state.deadlineEpochMillis ?: NO_DEADLINE)
            .putLong(KEY_REMAINING, state.remainingMillis)
            .putBoolean(KEY_SOUND_ENABLED, state.soundEnabled)
            .putBoolean(KEY_EXPLAINER_SEEN, state.reminderExplainerSeen)
            .putBoolean(KEY_NOTIFICATION_POSTED, state.completionNotificationPosted)
            .commit()
    }

    private companion object {
        private const val PREFERENCES_NAME = "ritual_timer"
        private const val KEY_PRESET = "preset"
        private const val KEY_STATUS = "status"
        private const val KEY_DEADLINE = "deadline_epoch_millis"
        private const val KEY_REMAINING = "remaining_millis"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_EXPLAINER_SEEN = "reminder_explainer_seen"
        private const val KEY_NOTIFICATION_POSTED = "completion_notification_posted"
        private const val NO_DEADLINE = Long.MIN_VALUE
    }
}
