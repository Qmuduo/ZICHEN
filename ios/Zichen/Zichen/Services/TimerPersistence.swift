import Foundation

struct TimerPersistence {
    private let defaults: UserDefaults
    private let key = "ritualTimer.state.v1"

    init(defaults: UserDefaults = .standard) { self.defaults = defaults }

    func load() -> RitualTimerState {
        guard let data = defaults.data(forKey: key),
              let state = try? JSONDecoder().decode(RitualTimerState.self, from: data) else {
            return .ready()
        }
        return state
    }

    func save(_ state: RitualTimerState) {
        guard let data = try? JSONEncoder().encode(state) else { return }
        defaults.set(data, forKey: key)
    }
}
