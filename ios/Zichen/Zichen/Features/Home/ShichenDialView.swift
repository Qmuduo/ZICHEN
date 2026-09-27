import SwiftUI

struct ShichenDialView: View {
    let now: Date
    let previewedIndex: Int?
    let onSelect: (Int) -> Void

    private let branches = Array("子丑寅卯辰巳午未申酉戌亥")
    private let stems = Array("甲乙丙丁戊己庚辛壬癸")

    var body: some View {
        GeometryReader { proxy in
            let side = min(proxy.size.width, proxy.size.height)
            let center = CGPoint(x: side / 2, y: side / 2)
            let unit = side / 360
            ZStack {
                Canvas { context, size in drawDial(context: &context, size: size) }
                ForEach(0..<12, id: \.self) { index in
                    let point = point(center: center, radius: 120 * unit, degrees: -90 + Double(index) * 30)
                    Text(String(branches[index]))
                        .font(AppTheme.serif(max(11, 14 * unit), weight: index == currentIndex ? .medium : .regular))
                        .foregroundStyle(index == previewedIndex ? AppTheme.preview : (index == currentIndex ? AppTheme.goldLight : AppTheme.text))
                        .position(point)
                        .accessibilityHidden(true)
                }
                ForEach(0..<24, id: \.self) { index in
                    let point = point(center: center, radius: 140 * unit, degrees: -97.5 + Double(index) * 15)
                    Text(index.isMultiple(of: 2) ? "初" : "正")
                        .font(AppTheme.serif(max(8, 10 * unit)))
                        .foregroundStyle(AppTheme.muted)
                        .position(point)
                        .accessibilityHidden(true)
                }
                ForEach(0..<10, id: \.self) { index in
                    let point = point(center: center, radius: 99 * unit, degrees: -90 + Double(index) * 36)
                    Text(String(stems[index]))
                        .font(AppTheme.serif(max(8, 10 * unit)))
                        .foregroundStyle(AppTheme.muted.opacity(0.9))
                        .position(point)
                        .accessibilityHidden(true)
                }
            }
            .frame(width: side, height: side)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .contentShape(Rectangle())
            .gesture(DragGesture(minimumDistance: 0).onEnded { value in
                if let index = DialGeometry.sectorAt(point: value.location, center: center, radius: 170 * unit) {
                    onSelect(index)
                }
            })
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("十二时辰盘")
            .accessibilityValue(previewedIndex.map { "预览\(branches[$0])时" } ?? "当前\(branches[currentIndex])时")
            .accessibilityHint("轻点外圈预览一个时辰")
            .accessibilityAdjustableAction { direction in
                let base = previewedIndex ?? currentIndex
                switch direction {
                case .increment: onSelect((base + 1) % 12)
                case .decrement: onSelect((base + 11) % 12)
                @unknown default: break
                }
            }
        }
        .frame(height: 320)
    }

    private var currentIndex: Int { TraditionalTime.read(date: now).shichen.index }

    private func drawDial(context: inout GraphicsContext, size: CGSize) {
        let side = min(size.width, size.height)
        let unit = side / 360
        let center = CGPoint(x: side / 2, y: side / 2)
        let outer = 174 * unit
        let rimRect = CGRect(x: center.x - outer, y: center.y - outer, width: outer * 2, height: outer * 2)
        context.fill(Path(ellipseIn: rimRect), with: .radialGradient(
            Gradient(colors: [AppTheme.surface, AppTheme.ink]), center: CGPoint(x: center.x - 42 * unit, y: center.y - 58 * unit), startRadius: 0, endRadius: 240 * unit))
        context.stroke(Path(ellipseIn: rimRect), with: .color(AppTheme.gold.opacity(0.25)), lineWidth: 0.7 * unit)
        context.stroke(Path(ellipseIn: CGRect(x: center.x - 89 * unit, y: center.y - 89 * unit, width: 178 * unit, height: 178 * unit)), with: .color(AppTheme.rule.opacity(0.55)), lineWidth: 0.6 * unit)

        if let previewedIndex { drawSector(previewedIndex, center: center, unit: unit, context: &context) }
        for tick in 0..<96 {
            let shichenBoundary = tick.isMultiple(of: 8)
            let halfHourBoundary = tick.isMultiple(of: 4)
            let inner = CGFloat(shichenBoundary ? 143 : (halfHourBoundary ? 148 : 152)) * unit
            let angle = -105 + Double(tick) * 3.75
            var path = Path()
            path.move(to: point(center: center, radius: inner, degrees: angle))
            path.addLine(to: point(center: center, radius: 164 * unit, degrees: angle))
            let tint = shichenBoundary ? AppTheme.text.opacity(0.9) : (halfHourBoundary ? AppTheme.muted : AppTheme.rule.opacity(0.85))
            context.stroke(path, with: .color(tint), lineWidth: (shichenBoundary ? 1.2 : (halfHourBoundary ? 0.9 : 0.6)) * unit)
        }
        drawArc(currentIndex, center: center, unit: unit, context: &context)
        drawGnomon(center: center, unit: unit, context: &context)
    }

    private func drawSector(_ index: Int, center: CGPoint, unit: CGFloat, context: inout GraphicsContext) {
        let middle = -90 + Double(index) * 30
        let start = middle - 12.5
        var path = Path()
        path.move(to: point(center: center, radius: 164 * unit, degrees: start))
        path.addArc(center: center, radius: 164 * unit, startAngle: .degrees(start), endAngle: .degrees(start + 25), clockwise: false)
        path.addLine(to: point(center: center, radius: 108 * unit, degrees: start + 25))
        path.addArc(center: center, radius: 108 * unit, startAngle: .degrees(start + 25), endAngle: .degrees(start), clockwise: true)
        path.closeSubpath()
        context.fill(path, with: .color(AppTheme.preview.opacity(0.16)))
        context.stroke(path, with: .color(AppTheme.preview.opacity(0.5)), lineWidth: 0.7 * unit)
    }

    private func drawArc(_ index: Int, center: CGPoint, unit: CGFloat, context: inout GraphicsContext) {
        let middle = -90 + Double(index) * 30
        var path = Path()
        path.addArc(center: center, radius: 170 * unit, startAngle: .degrees(middle - 12.5), endAngle: .degrees(middle + 12.5), clockwise: false)
        context.stroke(path, with: .color(AppTheme.gold), style: StrokeStyle(lineWidth: 1.5 * unit, lineCap: .round))
    }

    private func drawGnomon(center: CGPoint, unit: CGFloat, context: inout GraphicsContext) {
        let parts = Calendar.current.dateComponents([.hour, .minute, .second, .nanosecond], from: now)
        let rotation = DialGeometry.pointerRotationDegrees(hour: parts.hour ?? 0, minute: parts.minute ?? 0, second: parts.second ?? 0, nanosecond: parts.nanosecond ?? 0)
        let angle = (-90 + rotation) * .pi / 180
        let perpendicular = angle + .pi / 2
        let alongX = CGFloat(cos(angle))
        let alongY = CGFloat(sin(angle))
        let acrossX = CGFloat(cos(perpendicular))
        let acrossY = CGFloat(sin(perpendicular))
        let tip = CGPoint(x: center.x + alongX * 88 * unit, y: center.y + alongY * 88 * unit)
        let base = CGPoint(x: center.x + alongX * 7 * unit, y: center.y + alongY * 7 * unit)
        let wingA = CGPoint(x: base.x + acrossX * 2.4 * unit, y: base.y + acrossY * 2.4 * unit)
        let wingB = CGPoint(x: base.x - acrossX * 2.4 * unit, y: base.y - acrossY * 2.4 * unit)
        var needle = Path()
        needle.move(to: tip); needle.addLine(to: wingA); needle.addLine(to: wingB); needle.closeSubpath()
        context.fill(needle, with: .linearGradient(Gradient(colors: [AppTheme.goldLight, AppTheme.gold.opacity(0.5)]), startPoint: tip, endPoint: base))
        let hub = CGRect(x: center.x - 7.2 * unit, y: center.y - 7.2 * unit, width: 14.4 * unit, height: 14.4 * unit)
        context.fill(Path(ellipseIn: hub), with: .color(AppTheme.surface))
        context.stroke(Path(ellipseIn: hub), with: .color(AppTheme.gold), lineWidth: unit)
        context.fill(Path(ellipseIn: CGRect(x: center.x - 2.2 * unit, y: center.y - 2.2 * unit, width: 4.4 * unit, height: 4.4 * unit)), with: .color(AppTheme.goldLight))
    }

    private func point(center: CGPoint, radius: CGFloat, degrees: Double) -> CGPoint {
        let angle = degrees * .pi / 180
        return CGPoint(x: center.x + CGFloat(cos(angle)) * radius, y: center.y + CGFloat(sin(angle)) * radius)
    }
}
