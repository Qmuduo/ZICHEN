import SwiftUI

enum AppRoute: Hashable {
    case timeDetail(Int)
    case lunarCalendar
    case clockCalendar
    case almanacDetail(Date)
    case timer
}

@MainActor
struct AppRootView: View {
    @Environment(\.scenePhase) private var scenePhase
    @State private var path: [AppRoute] = []
    @StateObject private var calendarStore = CalendarSelectionStore(today: .now)
    @StateObject private var timerStore = TimerStore()
    private let lunarProvider = LocalLunarCalendarProvider()

    var body: some View {
        NavigationStack(path: $path) {
            HomeView { route in path.append(route) }
                .toolbar(.hidden, for: .navigationBar)
                .navigationDestination(for: AppRoute.self) { route in
                    switch route {
                    case .timeDetail(let index):
                        TimeDetailView(index: index)
                            .navigationTitle("时辰笺")
                            .navigationBarTitleDisplayMode(.inline)
                    case .lunarCalendar:
                        LunarCalendarView(store: calendarStore, provider: lunarProvider) { route in
                            path.append(route)
                        }
                        .navigationTitle("农历月历")
                        .navigationBarTitleDisplayMode(.inline)
                    case .clockCalendar:
                        ClockCalendarView(store: calendarStore, provider: lunarProvider) { route in
                            path.append(route)
                        }
                        .navigationTitle("钟历")
                        .navigationBarTitleDisplayMode(.inline)
                    case .almanacDetail(let date):
                        AlmanacDetailView(day: lunarProvider.day(for: date))
                            .navigationTitle("日期详情")
                            .navigationBarTitleDisplayMode(.inline)
                    case .timer:
                        TimerView(store: timerStore)
                            .navigationTitle("仪式计时")
                            .navigationBarTitleDisplayMode(.inline)
                    }
                }
        }
        .tint(AppTheme.gold)
        .inkBackground()
        .onChange(of: scenePhase) { phase in
            if phase == .active { timerStore.refreshAfterForeground() }
        }
    }
}
