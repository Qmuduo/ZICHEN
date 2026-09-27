import Combine
import Foundation

@MainActor
final class TimerStore: ObservableObject {
    @Published private(set) var state: RitualTimerState
    @Published private(set) var now = Date.now
    @Published private(set) var notificationsAllowed = false
    @Published private(set) var statusMessage: String?

    private let persistence: TimerPersistence
    private let scheduler: TimerNotificationScheduler
    private var notificationGeneration = 0

    init(persistence: TimerPersistence = TimerPersistence(), scheduler: TimerNotificationScheduler = TimerNotificationScheduler()) {
        self.persistence = persistence
        self.scheduler = scheduler
        let restored = persistence.load()
        let restoredIdentifier = restored.notificationIdentifier
        state = restored.refresh(at: .now)
        scheduler.cancelLegacyCompletion()
        if state.status != .running, let restoredIdentifier {
            scheduler.cancelPendingCompletion(identifier: restoredIdentifier)
        }
        persistence.save(state)
        Task { refreshAfterForeground() }
        completeIfNeeded()
    }

    var shouldExplainReminders: Bool { !state.reminderExplainerSeen }
    var remaining: TimeInterval { state.remaining(at: now) }

    func start(withReminder: Bool = false) {
        let generation = invalidateNotificationTasks()
        let previousIdentifier = state.notificationIdentifier
        now = .now
        state = state.start(at: now)
        state.reminderEnabled = withReminder
        state.notificationIdentifier = withReminder ? UUID().uuidString : nil
        persist()
        statusMessage = nil
        if let previousIdentifier { scheduler.cancelPendingCompletion(identifier: previousIdentifier) }
        guard withReminder, let deadline = state.deadline,
              let identifier = state.notificationIdentifier else { return }
        Task {
            let allowed = await scheduler.requestAuthorizationIfNeeded()
            guard isNotificationRunCurrent(deadline, identifier: identifier, generation: generation) else { return }
            notificationsAllowed = allowed
            if allowed { await schedule(deadline: deadline, identifier: identifier, generation: generation) }
            else { statusMessage = "系统提醒未开启，计时仍会继续。可在设置中开启通知。" }
        }
    }

    func pause() {
        _ = invalidateNotificationTasks()
        let previousIdentifier = state.notificationIdentifier
        now = .now
        state = state.pause(at: now)
        persist()
        if let previousIdentifier { scheduler.cancelPendingCompletion(identifier: previousIdentifier) }
        completeIfNeeded()
    }

    func resume() {
        let generation = invalidateNotificationTasks()
        now = .now
        state = state.resume(at: now)
        if state.status == .running, state.reminderEnabled { state.notificationIdentifier = UUID().uuidString }
        persist()
        completeIfNeeded()
        if state.reminderEnabled, let deadline = state.deadline,
           let identifier = state.notificationIdentifier {
            Task {
                let allowed = await scheduler.notificationsAllowed()
                guard isNotificationRunCurrent(deadline, identifier: identifier, generation: generation) else { return }
                notificationsAllowed = allowed
                if allowed { await schedule(deadline: deadline, identifier: identifier, generation: generation) }
            }
        }
    }

    func reset() {
        _ = invalidateNotificationTasks()
        let previousIdentifier = state.notificationIdentifier
        state = state.reset()
        statusMessage = nil
        persist()
        if let previousIdentifier { scheduler.cancelPendingCompletion(identifier: previousIdentifier) }
    }

    func selectPreset(_ preset: TimerPreset) {
        state = state.selectPreset(preset)
        persist()
    }

    func toggleSound() {
        state.soundEnabled.toggle()
        guard state.status == .running, state.reminderEnabled, let deadline = state.deadline else {
            persist()
            return
        }
        let generation = invalidateNotificationTasks()
        let previousIdentifier = state.notificationIdentifier
        let identifier = UUID().uuidString
        state.notificationIdentifier = identifier
        persist()
        if let previousIdentifier { scheduler.cancelPendingCompletion(identifier: previousIdentifier) }
        Task {
            guard isNotificationRunCurrent(deadline, identifier: identifier, generation: generation) else { return }
            if notificationsAllowed { await schedule(deadline: deadline, identifier: identifier, generation: generation) }
        }
    }

    func tick() {
        now = .now
        guard state.status == .running else { return }
        let previousIdentifier = state.notificationIdentifier
        state = state.refresh(at: now)
        persist()
        if state.status == .finished {
            _ = invalidateNotificationTasks()
            if let previousIdentifier { scheduler.cancelPendingCompletion(identifier: previousIdentifier) }
        }
        completeIfNeeded()
    }

    func refreshAfterForeground() {
        let generation = invalidateNotificationTasks()
        let previousIdentifier = state.notificationIdentifier
        now = .now
        state = state.refresh(at: now)
        if state.status == .running, state.reminderEnabled {
            state.notificationIdentifier = UUID().uuidString
        }
        let deadline = state.status == .running && state.reminderEnabled ? state.deadline : nil
        let identifier = deadline == nil ? nil : state.notificationIdentifier
        persist()
        if let previousIdentifier { scheduler.cancelPendingCompletion(identifier: previousIdentifier) }
        Task {
            let allowed = await scheduler.notificationsAllowed()
            guard generation == notificationGeneration else { return }
            notificationsAllowed = allowed
            if notificationsAllowed, let deadline, let identifier,
               isNotificationRunCurrent(deadline, identifier: identifier, generation: generation) {
                await schedule(deadline: deadline, identifier: identifier, generation: generation)
            }
        }
        completeIfNeeded()
    }

    func openNotificationSettings() { scheduler.openAppNotificationSettings() }

    private func schedule(deadline: Date, identifier: String, generation: Int) async {
        guard isNotificationRunCurrent(deadline, identifier: identifier, generation: generation) else { return }
        do {
            try await scheduler.schedule(preset: state.preset, deadline: deadline,
                                         soundEnabled: state.soundEnabled, identifier: identifier)
            if !isNotificationRunCurrent(deadline, identifier: identifier, generation: generation) {
                scheduler.cancelPendingCompletion(identifier: identifier)
            }
        } catch {
            if isNotificationRunCurrent(deadline, identifier: identifier, generation: generation) {
                statusMessage = "提醒暂时无法安排，计时仍会继续。"
            }
        }
    }

    private func invalidateNotificationTasks() -> Int {
        notificationGeneration += 1
        return notificationGeneration
    }

    private func isNotificationRunCurrent(_ deadline: Date, identifier: String, generation: Int) -> Bool {
        generation == notificationGeneration
            && state.status == .running
            && state.reminderEnabled
            && state.deadline == deadline
            && state.notificationIdentifier == identifier
    }

    private func completeIfNeeded() {
        guard state.status == .finished, !state.completionHandled else { return }
        state.completionHandled = true
        statusMessage = "计时完成"
        persist()
    }

    private func persist() { persistence.save(state) }
}
