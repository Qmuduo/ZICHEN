import Foundation

enum LocalGregorianCalendar {
    static var current: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = .current
        return calendar
    }

    static func dateText(_ date: Date, includesWeekday: Bool = false) -> String {
        let parts = current.dateComponents([.year, .month, .day, .weekday], from: date)
        let civilDate = "\(parts.year ?? 0)年\(parts.month ?? 0)月\(parts.day ?? 0)日"
        guard includesWeekday, let weekday = parts.weekday else { return civilDate }
        let weekdayName = Array("一二三四五六日")[(weekday + 5) % 7]
        return "\(civilDate) 星期\(weekdayName)"
    }
}

struct GregorianMonth: Equatable, Hashable, Codable {
    let year: Int
    let month: Int

    init(year: Int, month: Int) {
        precondition((1...12).contains(month))
        self.year = year
        self.month = month
    }

    init(date: Date, calendar: Calendar = LocalGregorianCalendar.current) {
        let components = calendar.dateComponents([.year, .month], from: date)
        self.init(year: components.year ?? 2000, month: components.month ?? 1)
    }

    func firstDay(calendar: Calendar = LocalGregorianCalendar.current) -> Date {
        calendar.date(from: DateComponents(year: year, month: month, day: 1))!
    }

    func dayCount(calendar: Calendar = LocalGregorianCalendar.current) -> Int {
        calendar.range(of: .day, in: .month, for: firstDay(calendar: calendar))?.count ?? 30
    }

    func addingMonths(_ value: Int) -> GregorianMonth {
        let calendar = LocalGregorianCalendar.current
        let date = calendar.date(byAdding: .month, value: value, to: firstDay(calendar: calendar))!
        return GregorianMonth(date: date, calendar: calendar)
    }
}

struct LunarCalendarMonth: Equatable {
    let month: GregorianMonth
    let cells: [LunarCalendarDay?]
}

struct LunarCalendarDay: Equatable, Identifiable {
    let date: Date
    let weekday: String
    var lunarYear: Int? = nil
    var lunarYearName: String? = nil
    var zodiac: String? = nil
    var lunarMonthName: String? = nil
    var lunarDayName: String? = nil
    var yearGanzhi: String? = nil
    var monthGanzhi: String? = nil
    var dayGanzhi: String? = nil
    var solarTerm: String? = nil
    var festivals: [String] = []
    var primaryCellFestival: String? = nil
    var yi: [String] = []
    var ji: [String] = []
    var nayin: String? = nil
    var clash: String? = nil
    var shaDirection: String? = nil
    var pengzuGan: String? = nil
    var pengzuZhi: String? = nil
    var xiDirection: String? = nil
    var fuDirection: String? = nil
    var caiDirection: String? = nil
    var auspiciousDeities: [String] = []
    var inauspiciousDeities: [String] = []
    var hasFullAlmanac = false

    var id: Date { date }
    var hasLunarData: Bool { lunarYear != nil }
    var gregorianDateText: String { LocalGregorianCalendar.dateText(date) }
    var lunarYearCaption: String? {
        guard let lunarYearName else { return nil }
        return "\(lunarYearName)年\(zodiac.map { " · \($0)年" } ?? "")"
    }
    var lunarDateText: String? {
        guard let lunarMonthName else { return nil }
        return "\(lunarMonthName)月\(lunarDayName ?? "")"
    }
    var eventLabels: [String] { (festivals + [solarTerm].compactMap { $0 }).uniqued }
    var cellLabel: String? { primaryCellFestival ?? solarTerm }
    var sourceDescription: String {
        "历法及民俗资料：6tail lunar-swift 1.1.8；农历年与生肖以正月初一换年，年柱以立春当天换年，月柱以节交接当天换月，日柱以本地日期零点换日；福神采用流派二。"
    }
}

extension Array where Element == String {
    var uniqued: [String] {
        var seen = Set<String>()
        return filter { seen.insert($0).inserted }
    }
}
