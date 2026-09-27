import Foundation
import LunarSwift

struct LocalLunarCalendarProvider: LunarCalendarProviding {
    func month(_ month: GregorianMonth) -> LunarCalendarMonth {
        let calendar = LocalGregorianCalendar.current
        let first = month.firstDay(calendar: calendar)
        let weekday = calendar.component(.weekday, from: first) // Sunday = 1
        let mondayOffset = (weekday + 5) % 7
        let count = month.dayCount(calendar: calendar)
        let cells = (0..<42).map { index -> LunarCalendarDay? in
            let day = index - mondayOffset + 1
            guard (1...count).contains(day),
                  let date = calendar.date(byAdding: .day, value: day - 1, to: first) else { return nil }
            return self.day(for: date)
        }
        return LunarCalendarMonth(month: month, cells: cells)
    }

    func day(for date: Date) -> LunarCalendarDay {
        let calendar = LocalGregorianCalendar.current
        let localDate = calendar.startOfDay(for: date)
        let parts = calendar.dateComponents([.year, .month, .day, .weekday], from: localDate)
        let weekdayIndex = ((parts.weekday ?? 2) + 5) % 7
        let weekday = "星期" + String(Array("一二三四五六日")[weekdayIndex])
        let empty = LunarCalendarDay(date: localDate, weekday: weekday)

        // The engine uses the historical Julian/Gregorian transition. Keep the
        // app's proleptic Gregorian civil date visible instead of changing it.
        guard let year = parts.year, let month = parts.month, let day = parts.day,
              year <= 9999,
              year > 1582 || (year == 1582 && (month > 10 || (month == 10 && day >= 15))) else { return empty }

        let solar = Solar.fromYmdHms(year: year, month: month, day: day)
        let lunar = solar.lunar
        let full = (2025...2028).contains(year)
        let primary = (lunar.festivals + solar.festivals).uniqued
        return LunarCalendarDay(
            date: localDate,
            weekday: weekday,
            lunarYear: lunar.year,
            lunarYearName: lunar.yearInGanZhi,
            zodiac: lunar.yearShengXiao,
            lunarMonthName: lunar.monthInChinese,
            lunarDayName: lunar.dayInChinese,
            yearGanzhi: lunar.yearInGanZhiByLiChun,
            monthGanzhi: lunar.monthInGanZhi,
            dayGanzhi: lunar.dayInGanZhi,
            solarTerm: lunar.jieQi.isEmpty ? nil : lunar.jieQi,
            festivals: (primary + lunar.otherFestivals + solar.otherFestivals).uniqued,
            primaryCellFestival: primary.first,
            yi: full ? lunar.dayYi : [],
            ji: full ? lunar.dayJi : [],
            nayin: full ? lunar.dayNaYin : nil,
            clash: full ? (lunar.dayChongDesc.isEmpty ? nil : "冲\(lunar.dayChongDesc)") : nil,
            shaDirection: full ? (lunar.daySha.isEmpty ? nil : lunar.daySha) : nil,
            pengzuGan: full ? (lunar.pengZuGan.isEmpty ? nil : lunar.pengZuGan) : nil,
            pengzuZhi: full ? (lunar.pengZuZhi.isEmpty ? nil : lunar.pengZuZhi) : nil,
            xiDirection: full ? (lunar.dayPositionXiDesc.isEmpty ? nil : lunar.dayPositionXiDesc) : nil,
            fuDirection: full ? (lunar.getDayPositionFuDesc(sect: 2).isEmpty ? nil : lunar.getDayPositionFuDesc(sect: 2)) : nil,
            caiDirection: full ? (lunar.dayPositionCaiDesc.isEmpty ? nil : lunar.dayPositionCaiDesc) : nil,
            auspiciousDeities: full ? lunar.dayJiShen : [],
            inauspiciousDeities: full ? lunar.dayXiongSha : [],
            hasFullAlmanac: full
        )
    }
}
