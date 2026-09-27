import SwiftUI

struct MonthHeaderView: View {
    @ObservedObject var store: CalendarSelectionStore
    let provider: any LunarCalendarProviding
    let isClockCalendar: Bool
    let onToggleCalendar: () -> Void

    private var todayLunar: String {
        provider.day(for: .now).lunarDateText.map { "今日农历  \($0)" } ?? "今日农历资料暂不可用"
    }

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 10) {
                Button { store.moveMonth(by: -1) } label: {
                    Image(systemName: "chevron.left").frame(width: 36, height: 40)
                }
                .accessibilityLabel("上一个月")
                VStack(spacing: 3) {
                    Text("\(store.month.year)年 \(store.month.month)月")
                        .font(AppTheme.serif(19, weight: .medium))
                        .foregroundStyle(AppTheme.text)
                    Text(todayLunar).font(AppTheme.serif(11)).foregroundStyle(AppTheme.muted)
                }
                .frame(maxWidth: .infinity)
                Button { store.moveMonth(by: 1) } label: {
                    Image(systemName: "chevron.right").frame(width: 36, height: 40)
                }
                .accessibilityLabel("下一个月")
                Button { store.selectToday() } label: {
                    Text("今天").font(AppTheme.serif(12)).padding(.horizontal, 11).frame(height: 32)
                        .overlay(Capsule().stroke(AppTheme.gold.opacity(0.48), lineWidth: 0.8))
                }
                .accessibilityHint("回到今天的月份和日期")
            }
            HStack {
                Text(isClockCalendar ? "钟历" : "农历月历")
                    .font(AppTheme.serif(13)).foregroundStyle(AppTheme.muted)
                Spacer()
                Button(action: onToggleCalendar) {
                    HStack(spacing: 6) {
                        Text(isClockCalendar ? "月历" : "钟历")
                        Image(systemName: "arrow.left.arrow.right").font(.system(size: 10))
                    }
                    .font(AppTheme.serif(12))
                    .foregroundStyle(AppTheme.gold)
                    .padding(.horizontal, 12).padding(.vertical, 7)
                    .background(AppTheme.gold.opacity(0.08), in: Capsule())
                }
                .accessibilityHint("切换日历视图并保留当前日期")
            }
        }
        .buttonStyle(.plain)
    }
}
