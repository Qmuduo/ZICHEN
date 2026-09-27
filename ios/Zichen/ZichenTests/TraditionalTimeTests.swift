import XCTest
@testable import Zichen

final class TraditionalTimeTests: XCTestCase {
    func testShichenChangesAtHourBoundary() {
        XCTAssertEqual(TraditionalTime.read(hour: 0, minute: 59).shichen.branch, "子")
        XCTAssertEqual(TraditionalTime.read(hour: 1, minute: 0).shichen.branch, "丑")
        XCTAssertEqual(TraditionalTime.read(hour: 23, minute: 59).shichen.branch, "子")
    }

    func testQuarterAndNightWatchLabels() {
        XCTAssertEqual(TraditionalTime.read(hour: 23, minute: 15).quarterLabel, "初二刻")
        XCTAssertEqual(TraditionalTime.watch(hour: 19, minute: 0)?.label, "一更一点")
        XCTAssertNil(TraditionalTime.watch(hour: 5, minute: 0))
    }
}
