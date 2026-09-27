import SwiftUI

struct ClockCalendarView: View {
    @ObservedObject var store: CalendarSelectionStore
    let provider: any LunarCalendarProviding
    let open: (AppRoute) -> Void

    var body: some View {
        GeometryReader { proxy in
            let landscape = proxy.size.width > proxy.size.height
            Group {
                if landscape {
                    ScrollView(.vertical) {
                        HStack(alignment: .top, spacing: 12) {
                            clockPanel.compactMapFrame(width: proxy.size.width * 0.38)
                            VStack(spacing: 10) {
                                calendarPanel
                                CalendarDaySummary(day: provider.day(for: store.selectedDate), compact: true)
                            }
                        }
                        .padding(.vertical, 8)
                    }
                    .scrollIndicators(.hidden)
                    .padding(.horizontal, 12)
                } else {
                    ScrollView {
                        VStack(spacing: 12) {
                            clockPanel.frame(height: 240)
                            calendarPanel
                            CalendarDaySummary(day: provider.day(for: store.selectedDate), compact: true)
                            Text("点击日期只会更改所选日期")
                                .font(AppTheme.serif(10)).foregroundStyle(AppTheme.muted)
                                .padding(.bottom, 16)
                        }
                        .padding(.horizontal, 14).padding(.top, 8)
                    }
                    .scrollIndicators(.hidden)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .inkBackground()
        }
    }

    private var clockPanel: some View {
        TimelineView(.periodic(from: .now, by: 1)) { timeline in
            let now = timeline.date
            let reading = TraditionalTime.read(date: now)
            VStack(spacing: 6) {
                ShichenDialView(now: now, previewedIndex: nil) { _ in }
                    .frame(maxWidth: .infinity).frame(height: 210)
                Text("\(reading.shichen.branch)时 · \(reading.quarterLabel) · \(reading.clockText)")
                    .font(AppTheme.serif(14)).foregroundStyle(AppTheme.goldLight)
                    .lineLimit(1).minimumScaleFactor(0.8)
                Text(LocalGregorianCalendar.dateText(now, includesWeekday: true))
                    .font(AppTheme.data(10)).foregroundStyle(AppTheme.muted)
            }
        }
    }

    private var calendarPanel: some View {
        VStack(spacing: 9) {
            MonthHeaderView(store: store, provider: provider, isClockCalendar: true) {
                open(.lunarCalendar)
            }
            CalendarGridView(store: store, provider: provider)
        }
    }
}

private extension View {
    func compactMapFrame(width: CGFloat) -> some View { frame(width: width) }
}
