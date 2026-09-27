import SwiftUI

struct CalendarGridView: View {
    @ObservedObject var store: CalendarSelectionStore
    let provider: any LunarCalendarProviding

    private let weekdays = Array("一二三四五六日")
    private let columns = Array(repeating: GridItem(.flexible(minimum: 0), spacing: 1), count: 7)
    private var calendarMonth: LunarCalendarMonth { provider.month(store.month) }

    var body: some View {
        VStack(spacing: 5) {
            LazyVGrid(columns: columns, spacing: 2) {
                ForEach(0..<7, id: \.self) { index in
                    Text(String(weekdays[index]))
                        .font(AppTheme.serif(11))
                        .foregroundStyle(index == 6 ? AppTheme.warning.opacity(0.82) : AppTheme.muted)
                        .frame(maxWidth: .infinity).frame(height: 22)
                        .accessibilityHidden(true)
                }
                ForEach(Array(calendarMonth.cells.enumerated()), id: \.offset) { item in
                    if let day = item.element { dateCell(day) }
                    else { Color.clear.frame(height: 53).accessibilityHidden(true) }
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityLabel("\(store.month.year)年\(store.month.month)月日历，周一开始")
    }

    private func dateCell(_ day: LunarCalendarDay) -> some View {
        let calendar = LocalGregorianCalendar.current
        let isSelected = calendar.isDate(day.date, inSameDayAs: store.selectedDate)
        let isToday = calendar.isDate(day.date, inSameDayAs: store.today)
        let dayNumber = calendar.component(.day, from: day.date)
        let subtitle = day.cellLabel ?? day.lunarDayName ?? ""
        return Button { store.selectDate(day.date) } label: {
            VStack(spacing: 3) {
                Text("\(dayNumber)")
                    .font(AppTheme.data(13, weight: isSelected ? .semibold : .regular))
                    .foregroundStyle(isSelected ? AppTheme.ink : (isToday ? AppTheme.goldLight : AppTheme.text))
                Text(subtitle.isEmpty ? " " : subtitle)
                    .font(AppTheme.serif(8))
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
                    .foregroundStyle(isSelected ? AppTheme.ink.opacity(0.8) : (day.cellLabel == nil ? AppTheme.muted : AppTheme.gold))
            }
            .frame(maxWidth: .infinity).frame(height: 53)
            .background {
                if isSelected { RoundedRectangle(cornerRadius: 9).fill(AppTheme.gold) }
                else if isToday { RoundedRectangle(cornerRadius: 9).stroke(AppTheme.gold.opacity(0.65), lineWidth: 0.8) }
            }
            .contentShape(RoundedRectangle(cornerRadius: 9))
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(day.gregorianDateText)，\(day.lunarDateText ?? "农历资料暂不可用")\(day.cellLabel.map { "，\($0)" } ?? "")")
        .accessibilityValue(isSelected ? "已选中" : (isToday ? "今天" : ""))
    }
}

struct CalendarDaySummary: View {
    let day: LunarCalendarDay
    var compact = false

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text(day.gregorianDateText).font(AppTheme.data(compact ? 12 : 14)).foregroundStyle(AppTheme.text)
                Text(day.weekday).font(AppTheme.serif(11)).foregroundStyle(AppTheme.muted)
            }
            if let lunar = day.lunarDateText {
                Text("农历  \(lunar)")
                    .font(AppTheme.serif(compact ? 14 : 17))
                    .foregroundStyle(AppTheme.goldLight)
            } else {
                Text("该日期暂无可靠农历换算资料")
                    .font(AppTheme.serif(13)).foregroundStyle(AppTheme.muted)
            }
            if let caption = day.lunarYearCaption {
                Text("\(caption)  ·  \(day.yearGanzhi ?? "")年  \(day.monthGanzhi ?? "")月  \(day.dayGanzhi ?? "")日")
                    .font(AppTheme.serif(11)).foregroundStyle(AppTheme.muted)
            }
            if !day.eventLabels.isEmpty {
                Text(day.eventLabels.joined(separator: " · "))
                    .font(AppTheme.serif(12)).foregroundStyle(AppTheme.gold)
            }
            if day.hasFullAlmanac {
                HStack(alignment: .top, spacing: 8) {
                    Text("宜  \(day.yi.isEmpty ? "暂无资料" : day.yi.joined(separator: "　"))")
                        .foregroundStyle(AppTheme.preview)
                    Text("忌  \(day.ji.isEmpty ? "暂无资料" : day.ji.joined(separator: "　"))")
                        .foregroundStyle(AppTheme.warning)
                }
                .font(AppTheme.serif(compact ? 10 : 12))
                .fixedSize(horizontal: false, vertical: true)
            } else {
                Text("完整黄历资料覆盖 2025—2028 年")
                    .font(AppTheme.serif(11)).foregroundStyle(AppTheme.muted)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(AppTheme.surface.opacity(0.68), in: RoundedRectangle(cornerRadius: 13))
        .overlay(RoundedRectangle(cornerRadius: 13).stroke(AppTheme.rule.opacity(0.34), lineWidth: 0.7))
        .accessibilityElement(children: .combine)
    }
}
