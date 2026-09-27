package com.qmuduo.zichen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.qmuduo.zichen.ui.theme.inkSurface
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.qmuduo.zichen.calendar.LocalLunarCalendarProvider
import com.qmuduo.zichen.time.TraditionalTime
import com.qmuduo.zichen.timer.TimerCoordinator
import com.qmuduo.zichen.timer.TimerNotifications
import com.qmuduo.zichen.timer.TimerPreset
import com.qmuduo.zichen.timer.TimerStatus
import com.qmuduo.zichen.ui.date.AlmanacDetailScreen
import com.qmuduo.zichen.ui.date.ClockCalendarScreen
import com.qmuduo.zichen.ui.date.LunarCalendarScreen
import com.qmuduo.zichen.ui.detail.TimeDetailScreen
import com.qmuduo.zichen.ui.home.HomeScreen
import com.qmuduo.zichen.ui.timer.TimerScreen
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth

private enum class AppPage {
    HOME,
    TIME_DETAIL,
    DATE_DETAIL,
    CLOCK_CALENDAR,
    ALMANAC_DETAIL,
    TIMER,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent {
            ZichenApp()
        }
    }
}

@Composable
private fun ZichenApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val timerCoordinator = remember(context) { TimerCoordinator(context.applicationContext) }
    val timerNotifications = remember(context) { TimerNotifications(context.applicationContext) }
    val calendarProvider = remember { LocalLunarCalendarProvider() }
    var today by remember { mutableStateOf(LocalDate.now()) }

    var page by rememberSaveable { mutableStateOf(AppPage.HOME.name) }
    var calendarMonthIso by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    var selectedDateIso by rememberSaveable { mutableStateOf(today.toString()) }
    val calendarMonth = remember(calendarMonthIso) { YearMonth.parse(calendarMonthIso) }
    val selectedDate = remember(selectedDateIso) { LocalDate.parse(selectedDateIso) }
    val onCalendarMonthChange: (YearMonth) -> Unit = { month ->
        calendarMonthIso = month.toString()
        selectedDateIso = month.atDay(selectedDate.dayOfMonth.coerceAtMost(month.lengthOfMonth())).toString()
    }
    val onSelectCalendarDate: (LocalDate) -> Unit = { date -> selectedDateIso = date.toString() }
    val onCalendarToday: () -> Unit = {
        val currentDate = LocalDate.now()
        today = currentDate
        calendarMonthIso = YearMonth.from(currentDate).toString()
        selectedDateIso = currentDate.toString()
    }
    var previewedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var detailIndex by rememberSaveable { mutableIntStateOf(0) }
    var timerState by remember { mutableStateOf(timerCoordinator.restore()) }
    var nowEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notificationsAllowed by remember { mutableStateOf(timerNotifications.canPost(timerState.soundEnabled)) }
    var exactAlarmAllowed by remember { mutableStateOf(timerCoordinator.exactAlarmAllowed()) }

    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationsAllowed = timerNotifications.canPost(timerState.soundEnabled)
        if (granted) {
            openExactAlarmSettingsIfNeeded(context, timerCoordinator)
        }
        timerState = timerCoordinator.refresh()
    }

    val requestReminderPermissions = {
        timerNotifications.ensureChannels()
        val notificationPermissionMissing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        when {
            notificationPermissionMissing -> requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            !timerNotifications.canPost(timerState.soundEnabled) -> context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
            )
            else -> openExactAlarmSettingsIfNeeded(context, timerCoordinator)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                today = LocalDate.now()
                notificationsAllowed = timerNotifications.canPost(timerState.soundEnabled)
                exactAlarmAllowed = timerCoordinator.exactAlarmAllowed()
                nowEpochMillis = System.currentTimeMillis()
                timerState = timerCoordinator.restore(nowEpochMillis)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(timerState.status, timerState.deadlineEpochMillis) {
        if (timerState.status == TimerStatus.RUNNING) {
            while (true) {
                delay(1_000L)
                nowEpochMillis = System.currentTimeMillis()
                timerState = timerCoordinator.refresh(nowEpochMillis)
                if (timerState.status != TimerStatus.RUNNING) break
            }
        }
    }

    LaunchedEffect(page) {
        if (page == AppPage.DATE_DETAIL.name || page == AppPage.CLOCK_CALENDAR.name) {
            while (true) {
                today = LocalDate.now()
                delay(60_000L)
            }
        }
    }

    BackHandler(enabled = page != AppPage.HOME.name) {
        page = when (AppPage.valueOf(page)) {
            AppPage.CLOCK_CALENDAR, AppPage.ALMANAC_DETAIL -> AppPage.DATE_DETAIL.name
            else -> AppPage.HOME.name
        }
    }

    val pageOpacity = remember(page) { Animatable(0f) }
    LaunchedEffect(page) { pageOpacity.animateTo(1f, tween(180)) }
    Box(Modifier.fillMaxSize().inkSurface()) {
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = pageOpacity.value }) {
            when (AppPage.valueOf(page)) {
                AppPage.HOME -> HomeScreen(
                    previewedIndex = previewedIndex,
                    onPreviewedIndexChange = { previewedIndex = it },
                    onOpenTimeDetail = { index ->
                        detailIndex = index
                        page = AppPage.TIME_DETAIL.name
                    },
                    onOpenDate = { page = AppPage.DATE_DETAIL.name },
                    onOpenTimer = { page = AppPage.TIMER.name },
                )

                AppPage.TIME_DETAIL -> TimeDetailScreen(
                    shichen = TraditionalTime.shichen[detailIndex],
                    onBack = { page = AppPage.HOME.name },
                )

                AppPage.DATE_DETAIL -> LunarCalendarScreen(
                    month = calendarMonth,
                    selectedDate = selectedDate,
                    today = today,
                    provider = calendarProvider,
                    onBack = { page = AppPage.HOME.name },
                    onMonthChange = onCalendarMonthChange,
                    onSelectDate = onSelectCalendarDate,
                    onToday = onCalendarToday,
                    onOpenClockCalendar = { page = AppPage.CLOCK_CALENDAR.name },
                    onOpenDetails = { date ->
                        selectedDateIso = date.toString()
                        page = AppPage.ALMANAC_DETAIL.name
                    },
                )

                AppPage.CLOCK_CALENDAR -> ClockCalendarScreen(
                    month = calendarMonth,
                    selectedDate = selectedDate,
                    today = today,
                    provider = calendarProvider,
                    onMonthChange = onCalendarMonthChange,
                    onSelectDate = onSelectCalendarDate,
                    onToday = onCalendarToday,
                    onOpenMonthCalendar = { page = AppPage.DATE_DETAIL.name },
                )

                AppPage.ALMANAC_DETAIL -> AlmanacDetailScreen(
                    day = remember(calendarProvider, selectedDate) { calendarProvider.getDate(selectedDate) },
                    onBack = { page = AppPage.DATE_DETAIL.name },
                )

                AppPage.TIMER -> TimerScreen(
                    state = timerState,
                    nowEpochMillis = nowEpochMillis,
                    exactAlarmAllowed = exactAlarmAllowed,
                    notificationsAllowed = notificationsAllowed,
                    onStart = { preset -> timerState = timerCoordinator.start(preset) },
                    onPause = { timerState = timerCoordinator.pause() },
                    onResume = { timerState = timerCoordinator.resume() },
                    onReset = { timerState = timerCoordinator.reset() },
                    onSelectPreset = { preset -> timerState = timerCoordinator.selectPreset(preset) },
                    onToggleSound = { timerState = timerCoordinator.toggleSound() },
                    onRequestReminderPermissions = requestReminderPermissions,
                    onBack = { page = AppPage.HOME.name },
                )
            }
        }
    }
}

private fun openExactAlarmSettingsIfNeeded(context: Context, timerCoordinator: TimerCoordinator) {
    timerCoordinator.reminderSettingsIntent()?.let(context::startActivity)
}
