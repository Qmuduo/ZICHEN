import Foundation

protocol LunarCalendarProviding {
    func month(_ month: GregorianMonth) -> LunarCalendarMonth
    func day(for date: Date) -> LunarCalendarDay
}
