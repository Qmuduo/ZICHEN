import XCTest
@testable import Zichen

final class RitualTimerStateTests: XCTestCase {
    func testStartPauseAndResumePreserveRemainingDuration() {
        let start = Date(timeIntervalSince1970: 1_000)
        let running = RitualTimerState.ready(.oneQuarter).start(at: start)
        XCTAssertEqual(running.deadline, start.addingTimeInterval(900))
        let paused = running.pause(at: start.addingTimeInterval(300))
        XCTAssertEqual(paused.remaining, 600)
        let resumed = paused.resume(at: start.addingTimeInterval(500))
        XCTAssertEqual(resumed.deadline, start.addingTimeInterval(1_100))
    }

    func testRefreshFinishesAtAbsoluteDeadline() {
        let start = Date(timeIntervalSince1970: 1_000)
        let running = RitualTimerState.ready(.oneQuarter).start(at: start)
        XCTAssertEqual(running.refresh(at: start.addingTimeInterval(899)).status, .running)
        XCTAssertEqual(running.refresh(at: start.addingTimeInterval(900)).status, .finished)
    }
}
