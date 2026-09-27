import XCTest
@testable import Zichen

final class LunarCalendarProviderTests: XCTestCase {
    private let provider = LocalLunarCalendarProvider()

    func testSeptember2026UsesMondayFirst42CellGrid() {
        let month = provider.month(GregorianMonth(year: 2026, month: 9))
        XCTAssertEqual(month.cells.count, 42)
        XCTAssertNil(month.cells[0])
        XCTAssertTrue(LocalGregorianCalendar.current.isDate(month.cells[1]!.date, inSameDayAs: date(2026, 9, 1)))
    }

    func testFullAlmanacCoverageStartsAndEndsOnConfiguredDates() {
        XCTAssertTrue(provider.day(for: date(2025, 1, 1)).hasFullAlmanac)
        XCTAssertTrue(provider.day(for: date(2028, 12, 31)).hasFullAlmanac)
        XCTAssertFalse(provider.day(for: date(2024, 12, 31)).hasFullAlmanac)
        XCTAssertFalse(provider.day(for: date(2029, 1, 1)).hasFullAlmanac)
    }

    func testDateBeforeGregorianCutoverKeepsCivilDateWithoutLunarData() {
        let day = provider.day(for: date(1582, 10, 14))
        XCTAssertFalse(day.hasLunarData)
        XCTAssertEqual(day.gregorianDateText, "1582年10月14日")
    }

    func testRepresentativeDatesMatchAndroidCalendarFixtures() throws {
        let url = try XCTUnwrap(Bundle(for: Self.self).url(forResource: "calendar-parity", withExtension: "json"))
        let document = try XCTUnwrap(JSONSerialization.jsonObject(with: Data(contentsOf: url)) as? [String: Any])
        let fixtures = try XCTUnwrap(document["fixtures"] as? [[String: Any]])

        for fixture in fixtures {
            let parts = try XCTUnwrap((fixture["date"] as? String)?.split(separator: "-").compactMap { Int($0) })
            let actual = provider.day(for: date(parts[0], parts[1], parts[2]))
            XCTAssertEqual(actual.gregorianDateText, fixture["gregorianDateText"] as? String)
            XCTAssertEqual(actual.weekday, fixture["weekday"] as? String)
            XCTAssertEqual(actual.hasLunarData, fixture["hasLunarData"] as? Bool)
            XCTAssertEqual(actual.hasFullAlmanac, fixture["hasFullAlmanac"] as? Bool)
            XCTAssertEqual(actual.lunarYear, fixture["lunarYear"] as? Int)
            XCTAssertEqual(actual.lunarYearName, optionalString("lunarYearName", in: fixture))
            XCTAssertEqual(actual.zodiac, optionalString("zodiac", in: fixture))
            XCTAssertEqual(actual.lunarMonthName, optionalString("lunarMonthName", in: fixture))
            XCTAssertEqual(actual.lunarDayName, optionalString("lunarDayName", in: fixture))
            XCTAssertEqual(actual.yearGanzhi, optionalString("yearGanzhi", in: fixture))
            XCTAssertEqual(actual.monthGanzhi, optionalString("monthGanzhi", in: fixture))
            XCTAssertEqual(actual.dayGanzhi, optionalString("dayGanzhi", in: fixture))
            XCTAssertEqual(actual.solarTerm, optionalString("solarTerm", in: fixture))
            XCTAssertEqual(actual.festivals, fixture["festivals"] as? [String] ?? [])
            XCTAssertEqual(actual.primaryCellFestival, optionalString("primaryCellFestival", in: fixture))
            XCTAssertEqual(actual.yi, fixture["yi"] as? [String] ?? [])
            XCTAssertEqual(actual.ji, fixture["ji"] as? [String] ?? [])
            XCTAssertEqual(actual.nayin, optionalString("nayin", in: fixture))
            XCTAssertEqual(actual.clash, optionalString("clash", in: fixture))
            XCTAssertEqual(actual.shaDirection, optionalString("shaDirection", in: fixture))
            XCTAssertEqual(actual.pengzuGan, optionalString("pengzuGan", in: fixture))
            XCTAssertEqual(actual.pengzuZhi, optionalString("pengzuZhi", in: fixture))
            XCTAssertEqual(actual.xiDirection, optionalString("xiDirection", in: fixture))
            XCTAssertEqual(actual.fuDirection, optionalString("fuDirection", in: fixture))
            XCTAssertEqual(actual.caiDirection, optionalString("caiDirection", in: fixture))
            XCTAssertEqual(actual.auspiciousDeities, fixture["auspiciousDeities"] as? [String] ?? [])
            XCTAssertEqual(actual.inauspiciousDeities, fixture["inauspiciousDeities"] as? [String] ?? [])
        }
    }

    private func optionalString(_ key: String, in fixture: [String: Any]) -> String? {
        guard let value = fixture[key], !(value is NSNull) else { return nil }
        return value as? String
    }
}

private func date(_ year: Int, _ month: Int, _ day: Int) -> Date {
    let calendar = LocalGregorianCalendar.current
    return calendar.date(from: DateComponents(year: year, month: month, day: day))!
}
