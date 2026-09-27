import CoreGraphics
import Foundation

enum DialGeometry {
    static func pointerRotationDegrees(hour: Int, minute: Int, second: Int, nanosecond: Int) -> Double {
        let hoursFromZiStart = (hour - 23 + 24) % 24
        let elapsedMinutes = Double(hoursFromZiStart * 60 + minute) + Double(second) / 60 + Double(nanosecond) / 60_000_000_000
        return -15 + elapsedMinutes / 4
    }

    static func sectorAt(point: CGPoint, center: CGPoint, radius: CGFloat) -> Int? {
        let dx = point.x - center.x
        let dy = point.y - center.y
        let distance = hypot(dx, dy)
        let innerRadius = radius * 108 / 170
        guard radius > 0, distance >= innerRadius, distance <= radius else { return nil }
        let angleFromRight = atan2(dy, dx) * 180 / .pi
        let clockwiseFromTop = (angleFromRight + 90 + 360).truncatingRemainder(dividingBy: 360)
        return Int(floor((clockwiseFromTop + 15) / 30)) % 12
    }
}
