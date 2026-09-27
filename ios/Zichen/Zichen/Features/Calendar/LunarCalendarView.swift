import SwiftUI

struct LunarCalendarView: View {
    @ObservedObject var store: CalendarSelectionStore
    let provider: any LunarCalendarProviding
    let open: (AppRoute) -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                MonthHeaderView(store: store, provider: provider, isClockCalendar: false) {
                    open(.clockCalendar)
                }
                .padding(.horizontal, 18)
                CalendarGridView(store: store, provider: provider).padding(.horizontal, 15)
                CalendarDaySummary(day: provider.day(for: store.selectedDate))
                    .padding(.horizontal, 18)
                Button { open(.almanacDetail(store.selectedDate)) } label: {
                    HStack {
                        Text("查看黄历详情").font(AppTheme.serif(14))
                        Spacer()
                        Image(systemName: "arrow.up.right").font(.system(size: 11))
                    }
                    .foregroundStyle(AppTheme.goldLight)
                    .padding(14)
                    .background(AppTheme.gold.opacity(0.08), in: RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
                .padding(.horizontal, 18)
                Text("传统民俗资料，仅供参考")
                    .font(AppTheme.serif(10)).foregroundStyle(AppTheme.muted.opacity(0.8))
                    .padding(.bottom, 20)
            }
            .padding(.top, 12)
        }
        .scrollIndicators(.hidden)
        .inkBackground()
    }
}
