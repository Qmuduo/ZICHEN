import XCTest
@testable import Zichen

final class DialGeometryTests: XCTestCase {
    func testPointerRotationMatchesAndroidReference() {
        XCTAssertEqual(DialGeometry.pointerRotationDegrees(hour: 23, minute: 0, second: 0, nanosecond: 0), -15, accuracy: 0.001)
        XCTAssertEqual(DialGeometry.pointerRotationDegrees(hour: 0, minute: 0, second: 0, nanosecond: 0), 0, accuracy: 0.001)
        XCTAssertEqual(DialGeometry.pointerRotationDegrees(hour: 12, minute: 0, second: 0, nanosecond: 0), 180, accuracy: 0.001)
    }

    func testSectorHitIgnoresCenterAndOutside() {
        XCTAssertNil(DialGeometry.sectorAt(point: CGPoint(x: 100, y: 100), center: CGPoint(x: 100, y: 100), radius: 80))
        XCTAssertNil(DialGeometry.sectorAt(point: CGPoint(x: 190, y: 100), center: CGPoint(x: 100, y: 100), radius: 80))
        XCTAssertEqual(DialGeometry.sectorAt(point: CGPoint(x: 100, y: 25), center: CGPoint(x: 100, y: 100), radius: 80), 0)
    }
}
