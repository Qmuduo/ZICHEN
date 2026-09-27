import XCTest
@testable import Zichen

final class CalendarSelectionStoreTests: XCTestCase {
    func testMonthChangeClampsSelectedDay() {
        let store = CalendarSelectionStore(today: date(2026, 1, 31))
        store.selectMonth(GregorianMonth(year: 2026, month: 2))
        XCTAssertTrue(LocalGregorianCalendar.current.isDate(store.selectedDate, inSameDayAs: date(2026, 2, 28)))
    }

    func testTodayRestoresMonthAndSelection() {
        let today = date(2026, 3, 12)
        let store = CalendarSelectionStore(today: today)
        store.selectMonth(GregorianMonth(year: 2026, month: 7))
        store.selectDate(date(2026, 7, 2))
        store.selectToday(now: today)
        XCTAssertEqual(store.month, GregorianMonth(year: 2026, month: 3))
        XCTAssertTrue(LocalGregorianCalendar.current.isDate(store.selectedDate, inSameDayAs: today))
    }
}

private func date(_ year: Int, _ month: Int, _ day: Int) -> Date {
    let calendar = LocalGregorianCalendar.current
    return calendar.date(from: DateComponents(year: year, month: month, day: day))!
}
