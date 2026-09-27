import Foundation

enum TimerPreset: String, Codable, CaseIterable, Identifiable {
    case oneQuarter
    case incense
    case halfShichen

    var id: String { rawValue }
    var duration: TimeInterval {
        switch self {
        case .oneQuarter: return 15 * 60
        case .incense: return 30 * 60
        case .halfShichen: return 60 * 60
        }
    }
    var title: String {
        switch self {
        case .oneQuarter: return "一刻"
        case .incense: return "一炷香"
        case .halfShichen: return "半个时辰"
        }
    }
    var durationLabel: String {
        switch self {
        case .oneQuarter: return "15 分钟"
        case .incense: return "30 分钟"
        case .halfShichen: return "60 分钟"
        }
    }
}

enum TimerStatus: String, Codable {
    case ready
    case running
    case paused
    case finished
}

struct RitualTimerState: Codable, Equatable {
    var preset: TimerPreset
    var status: TimerStatus
    var deadline: Date?
    var remaining: TimeInterval
    var soundEnabled: Bool
    var reminderEnabled: Bool
    var notificationIdentifier: String? = nil
    var reminderExplainerSeen: Bool
    var completionHandled: Bool

    static func ready(_ preset: TimerPreset = .oneQuarter) -> RitualTimerState {
        RitualTimerState(preset: preset, status: .ready, deadline: nil,
                         remaining: preset.duration, soundEnabled: false, reminderEnabled: false,
                         reminderExplainerSeen: false, completionHandled: false)
    }

    func remaining(at now: Date) -> TimeInterval {
        guard status == .running, let deadline else { return remaining }
        return max(0, deadline.timeIntervalSince(now))
    }

    func start(at now: Date) -> RitualTimerState {
        var next = self
        next.status = .running
        next.deadline = now.addingTimeInterval(preset.duration)
        next.remaining = preset.duration
        next.completionHandled = false
        next.reminderExplainerSeen = true
        next.notificationIdentifier = nil
        return next
    }

    func pause(at now: Date) -> RitualTimerState {
        guard status == .running else { return self }
        let left = remaining(at: now)
        var next = self
        next.remaining = left
        next.status = left == 0 ? .finished : .paused
        next.deadline = nil
        next.notificationIdentifier = nil
        return next
    }

    func resume(at now: Date) -> RitualTimerState {
        guard status == .paused else { return self }
        if remaining <= 0 {
            var next = self
            next.status = .finished
            next.deadline = nil
            return next
        }
        var next = self
        next.status = .running
        next.deadline = now.addingTimeInterval(remaining)
        next.notificationIdentifier = nil
        return next
    }

    func refresh(at now: Date) -> RitualTimerState {
        guard status == .running else { return self }
        let left = remaining(at: now)
        var next = self
        next.remaining = left
        if left == 0 {
            next.status = .finished
            next.deadline = nil
            next.notificationIdentifier = nil
        }
        return next
    }

    func reset() -> RitualTimerState {
        var next = self
        next.status = .ready
        next.deadline = nil
        next.notificationIdentifier = nil
        next.remaining = preset.duration
        next.completionHandled = false
        return next
    }

    func selectPreset(_ newPreset: TimerPreset) -> RitualTimerState {
        guard status == .ready || status == .finished else { return self }
        var next = self
        next.preset = newPreset
        next.status = .ready
        next.deadline = nil
        next.notificationIdentifier = nil
        next.remaining = newPreset.duration
        next.completionHandled = false
        return next
    }
}
