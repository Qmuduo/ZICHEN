import Foundation
import Combine

final class CalendarSelectionStore: ObservableObject {
    @Published private(set) var month: GregorianMonth
    @Published private(set) var selectedDate: Date
    @Published private(set) var today: Date
    private var selectedCivilDate: DateComponents
    private var isShowingToday = true
    private var timeZoneObserver: NSObjectProtocol?

    init(today: Date = .now) {
        let calendar = LocalGregorianCalendar.current
        let normalized = calendar.startOfDay(for: today)
        self.today = normalized
        self.selectedDate = normalized
        self.month = GregorianMonth(date: normalized)
        self.selectedCivilDate = calendar.dateComponents([.year, .month, .day], from: normalized)
        timeZoneObserver = NotificationCenter.default.addObserver(
            forName: .NSSystemTimeZoneDidChange,
            object: nil,
            queue: .main
        ) { [weak self] _ in self?.restoreCivilSelectionAfterTimeZoneChange() }
    }

    deinit {
        if let timeZoneObserver { NotificationCenter.default.removeObserver(timeZoneObserver) }
    }

    func selectMonth(_ newMonth: GregorianMonth) {
        let calendar = LocalGregorianCalendar.current
        let parts = calendar.dateComponents([.day], from: selectedDate)
        let safeDay = min(parts.day ?? 1, newMonth.dayCount(calendar: calendar))
        month = newMonth
        selectedDate = calendar.date(from: DateComponents(year: newMonth.year, month: newMonth.month, day: safeDay))!
        selectedCivilDate = DateComponents(year: newMonth.year, month: newMonth.month, day: safeDay)
        isShowingToday = false
    }

    func moveMonth(by amount: Int) { selectMonth(month.addingMonths(amount)) }

    func selectDate(_ date: Date) {
        let calendar = LocalGregorianCalendar.current
        selectedDate = calendar.startOfDay(for: date)
        selectedCivilDate = calendar.dateComponents([.year, .month, .day], from: selectedDate)
        isShowingToday = calendar.isDate(selectedDate, inSameDayAs: today)
    }

    func selectToday(now: Date = .now) {
        let calendar = LocalGregorianCalendar.current
        let normalized = calendar.startOfDay(for: now)
        today = normalized
        selectedDate = normalized
        month = GregorianMonth(date: normalized)
        selectedCivilDate = calendar.dateComponents([.year, .month, .day], from: normalized)
        isShowingToday = true
    }

    private func restoreCivilSelectionAfterTimeZoneChange() {
        let calendar = LocalGregorianCalendar.current
        today = calendar.startOfDay(for: .now)
        if isShowingToday {
            selectedDate = today
            month = GregorianMonth(date: today, calendar: calendar)
            selectedCivilDate = calendar.dateComponents([.year, .month, .day], from: today)
        } else if let restored = calendar.date(from: selectedCivilDate) {
            selectedDate = calendar.startOfDay(for: restored)
        }
    }
}
