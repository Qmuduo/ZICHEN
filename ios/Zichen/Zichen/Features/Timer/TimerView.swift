import Combine
import SwiftUI

@MainActor
struct TimerView: View {
    @ObservedObject var store: TimerStore
    @State private var showReminderExplanation = false

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                VStack(spacing: 5) {
                    Eyebrow(text: "留一段安静")
                    Text("仪式计时").font(AppTheme.serif(25, weight: .medium)).foregroundStyle(AppTheme.goldLight)
                }
                presetPicker
                HourglassView(isRunning: store.state.status == .running)
                    .frame(width: 108, height: 132)
                    .accessibilityHidden(true)
                VStack(spacing: 7) {
                    Text(timeText(store.remaining))
                        .font(AppTheme.data(38, weight: .light))
                        .monospacedDigit()
                        .foregroundStyle(AppTheme.text)
                        .accessibilityLabel("剩余\(timeText(store.remaining))")
                    Text(statusTitle)
                        .font(AppTheme.serif(13)).foregroundStyle(statusColor)
                }
                actionButtons
                Toggle(isOn: Binding(get: { store.state.soundEnabled }, set: { _ in store.toggleSound() })) {
                    HStack(spacing: 10) {
                        Image(systemName: store.state.soundEnabled ? "bell.badge" : "bell.slash")
                            .foregroundStyle(AppTheme.gold)
                        VStack(alignment: .leading, spacing: 3) {
                            Text("结束时播放钟磬音").font(AppTheme.serif(13)).foregroundStyle(AppTheme.text)
                            Text("默认静音，可随时更改").font(AppTheme.serif(10)).foregroundStyle(AppTheme.muted)
                        }
                    }
                }
                .tint(AppTheme.gold)
                .padding(.horizontal, 4)
                if let message = store.statusMessage {
                    Text(message).font(AppTheme.serif(11)).foregroundStyle(AppTheme.muted)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                if !store.notificationsAllowed, store.state.reminderExplainerSeen {
                    Button("设置系统提醒") { store.openNotificationSettings() }
                        .font(AppTheme.serif(12)).foregroundStyle(AppTheme.gold)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                Text("离开 App 后由系统通知提醒；送达时间受通知设置、专注模式和系统调度影响。")
                    .font(AppTheme.serif(10)).lineSpacing(3).foregroundStyle(AppTheme.muted.opacity(0.85))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 25).padding(.top, 18).padding(.bottom, 30)
        }
        .scrollIndicators(.hidden)
        .inkBackground()
        .onReceive(Timer.publish(every: 1, on: .main, in: .common).autoconnect()) { _ in store.tick() }
        .sheet(isPresented: $showReminderExplanation) { reminderSheet }
    }

    private var presetPicker: some View {
        HStack(spacing: 7) {
            ForEach(TimerPreset.allCases) { preset in
                let selected = store.state.preset == preset
                Button { store.selectPreset(preset) } label: {
                    VStack(spacing: 5) {
                        Text(preset.title).font(AppTheme.serif(13))
                        Text(preset.durationLabel).font(AppTheme.data(9))
                    }
                    .foregroundStyle(selected ? AppTheme.goldLight : AppTheme.muted)
                    .frame(maxWidth: .infinity).padding(.vertical, 11)
                    .background(selected ? AppTheme.gold.opacity(0.11) : AppTheme.surface.opacity(0.65), in: RoundedRectangle(cornerRadius: 11))
                    .overlay(RoundedRectangle(cornerRadius: 11).stroke(selected ? AppTheme.gold.opacity(0.38) : AppTheme.rule.opacity(0.28), lineWidth: 0.8))
                }
                .buttonStyle(.plain)
                .disabled(store.state.status == .running || store.state.status == .paused)
                .accessibilityAddTraits(selected ? .isSelected : [])
            }
        }
    }

    @ViewBuilder private var actionButtons: some View {
        HStack(spacing: 12) {
            Button(action: secondaryAction) {
                Text(secondaryTitle)
                    .font(AppTheme.serif(14))
                    .foregroundStyle(AppTheme.text)
                    .frame(maxWidth: .infinity).frame(height: 48)
                    .overlay(Capsule().stroke(AppTheme.rule.opacity(0.55), lineWidth: 1))
            }
            .buttonStyle(.plain)
            Button(action: primaryAction) {
                Text(primaryTitle)
                    .font(AppTheme.serif(15, weight: .medium))
                    .foregroundStyle(AppTheme.ink)
                    .frame(maxWidth: .infinity).frame(height: 48)
                    .background(AppTheme.gold, in: Capsule())
            }
            .buttonStyle(.plain)
        }
    }

    private var reminderSheet: some View {
        VStack(spacing: 17) {
            Image(systemName: "bell.badge").font(.system(size: 27)).foregroundStyle(AppTheme.gold)
            Text("计时提醒").font(AppTheme.serif(21)).foregroundStyle(AppTheme.goldLight)
            Text("计时结束时，系统可以发送一条本地通知。是否允许通知由你决定；即使关闭，计时仍会继续。")
                .font(AppTheme.serif(13)).lineSpacing(5).foregroundStyle(AppTheme.text)
                .multilineTextAlignment(.center)
            Button {
                showReminderExplanation = false
                store.start(withReminder: true)
            } label: { Text("开始并设置提醒").font(AppTheme.serif(14)).foregroundStyle(AppTheme.ink).frame(maxWidth: .infinity).padding(.vertical, 13).background(AppTheme.gold, in: Capsule()) }
                .buttonStyle(.plain)
            Button {
                showReminderExplanation = false
                store.start(withReminder: false)
            } label: { Text("仅开始计时").font(AppTheme.serif(13)).foregroundStyle(AppTheme.text).padding(.vertical, 8) }
                .buttonStyle(.plain)
        }
        .padding(25)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTheme.ink.ignoresSafeArea())
        .presentationDetents([.medium])
        .presentationDragIndicator(.visible)
    }

    private var statusTitle: String {
        switch store.state.status {
        case .ready: return "\(store.state.preset.title) · 准备就绪"
        case .running: return "静心进行中"
        case .paused: return "计时已暂停"
        case .finished: return "一段时光，已然完成"
        }
    }

    private var statusColor: Color { store.state.status == .finished ? AppTheme.gold : AppTheme.muted }
    private var primaryTitle: String {
        switch store.state.status {
        case .ready: return "开始"
        case .running: return "暂停"
        case .paused: return "继续"
        case .finished: return "再计一次"
        }
    }
    private var secondaryTitle: String { "重置" }

    private func primaryAction() {
        switch store.state.status {
        case .ready:
            if store.shouldExplainReminders { showReminderExplanation = true }
            else { store.start(withReminder: store.notificationsAllowed) }
        case .running: store.pause()
        case .paused: store.resume()
        case .finished: store.start(withReminder: store.notificationsAllowed)
        }
    }

    private func secondaryAction() { store.reset() }

    private func timeText(_ value: TimeInterval) -> String {
        let total = max(0, Int(value.rounded(.up)))
        let hours = total / 3600
        let minutes = (total % 3600) / 60
        let seconds = total % 60
        return hours > 0 ? String(format: "%02d:%02d:%02d", hours, minutes, seconds) : String(format: "%02d:%02d", minutes, seconds)
    }
}

private struct HourglassView: View {
    let isRunning: Bool
    var body: some View {
        TimelineView(.animation(minimumInterval: 1, paused: !isRunning)) { timeline in
            let pulse = isRunning && Int(timeline.date.timeIntervalSinceReferenceDate).isMultiple(of: 2)
            Canvas { context, size in
                let inset: CGFloat = 12
                var shape = Path()
                shape.move(to: CGPoint(x: inset, y: inset))
                shape.addLine(to: CGPoint(x: size.width - inset, y: inset))
                shape.addCurve(to: CGPoint(x: size.width / 2, y: size.height / 2), control1: CGPoint(x: size.width * 0.28, y: size.height * 0.3), control2: CGPoint(x: size.width * 0.72, y: size.height * 0.3))
                shape.addCurve(to: CGPoint(x: inset, y: size.height - inset), control1: CGPoint(x: size.width * 0.72, y: size.height * 0.7), control2: CGPoint(x: size.width * 0.28, y: size.height * 0.7))
                shape.addLine(to: CGPoint(x: size.width - inset, y: size.height - inset))
                shape.addCurve(to: CGPoint(x: size.width / 2, y: size.height / 2), control1: CGPoint(x: size.width * 0.72, y: size.height * 0.7), control2: CGPoint(x: size.width * 0.28, y: size.height * 0.7))
                shape.addCurve(to: CGPoint(x: size.width - inset, y: inset), control1: CGPoint(x: size.width * 0.28, y: size.height * 0.3), control2: CGPoint(x: size.width * 0.72, y: size.height * 0.3))
                context.stroke(shape, with: .color(AppTheme.gold.opacity(0.78)), lineWidth: 1.4)
                let sand = Path(ellipseIn: CGRect(x: size.width * 0.40, y: size.height * (pulse ? 0.72 : 0.74), width: size.width * 0.2, height: size.height * 0.12))
                context.fill(sand, with: .color(AppTheme.gold.opacity(0.7)))
                var stream = Path()
                stream.move(to: CGPoint(x: size.width / 2, y: size.height * 0.47))
                stream.addLine(to: CGPoint(x: size.width / 2, y: size.height * 0.70))
                context.stroke(stream, with: .color(AppTheme.gold.opacity(isRunning ? 0.6 : 0.25)), lineWidth: 1)
                context.stroke(Path(roundedRect: CGRect(x: inset - 2, y: inset - 2, width: size.width - (inset - 2) * 2, height: 2), cornerRadius: 1), with: .color(AppTheme.gold), lineWidth: 1.5)
                context.stroke(Path(roundedRect: CGRect(x: inset - 2, y: size.height - inset, width: size.width - (inset - 2) * 2, height: 2), cornerRadius: 1), with: .color(AppTheme.gold), lineWidth: 1.5)
            }
        }
    }
}
