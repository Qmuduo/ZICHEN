import Foundation
import UIKit
import UserNotifications

struct TimerNotificationScheduler {
    static let legacyIdentifier = "ritual-timer-completion"
    private let center: UNUserNotificationCenter

    init(center: UNUserNotificationCenter = .current()) { self.center = center }

    func requestAuthorizationIfNeeded() async -> Bool {
        let settings = await center.notificationSettings()
        switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral:
            return settings.alertSetting == .enabled
        case .notDetermined:
            return (try? await center.requestAuthorization(options: [.alert, .sound])) ?? false
        case .denied:
            return false
        @unknown default:
            return false
        }
    }

    func notificationsAllowed() async -> Bool {
        let settings = await center.notificationSettings()
        return (settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional || settings.authorizationStatus == .ephemeral)
            && settings.alertSetting == .enabled
    }

    func schedule(preset: TimerPreset, deadline: Date, soundEnabled: Bool, identifier: String) async throws {
        let interval = max(1, deadline.timeIntervalSinceNow)
        let content = UNMutableNotificationContent()
        content.title = "时辰 · 计时完成"
        content.body = "\(preset.title)已结束。"
        content.sound = soundEnabled ? UNNotificationSound(named: UNNotificationSoundName(rawValue: "gentle_chime.wav")) : nil
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: interval, repeats: false)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        try await center.add(request)
    }

    func cancelPendingCompletion(identifier: String) {
        center.removePendingNotificationRequests(withIdentifiers: [identifier])
    }

    func cancelLegacyCompletion() {
        center.removePendingNotificationRequests(withIdentifiers: [Self.legacyIdentifier])
    }

    func openAppNotificationSettings() {
        guard let url = URL(string: UIApplication.openNotificationSettingsURLString) else { return }
        UIApplication.shared.open(url)
    }
}
