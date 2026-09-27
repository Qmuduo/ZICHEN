import Foundation

struct Shichen: Equatable, Identifiable {
    let index: Int
    let branch: String
    let alias: String
    let startHour: Int
    let poem: String

    var id: Int { index }
    var interval: String { "\(Self.hour(startHour)):00 — \(Self.hour((startHour + 2) % 24)):00" }

    private static func hour(_ value: Int) -> String { String(format: "%02d", value) }
}

struct TimeReading: Equatable {
    let shichen: Shichen
    let hour: Int
    let minute: Int
    let phase: String
    let quarterNumber: Int

    var clockText: String { String(format: "%02d:%02d", hour, minute) }
    var quarterLabel: String { "\(phase)\(TraditionalTime.chineseNumber(quarterNumber))刻" }
}

struct WatchReading: Equatable {
    let gengNumber: Int
    let pointNumber: Int

    var label: String {
        "\(TraditionalTime.chineseNumber(gengNumber))更\(TraditionalTime.chineseNumber(pointNumber))点"
    }
}

enum TraditionalTime {
    static let shichen: [Shichen] = [
        Shichen(index: 0, branch: "子", alias: "夜半", startHour: 23, poem: "归来仿佛三更"),
        Shichen(index: 1, branch: "丑", alias: "鸡鸣", startHour: 1, poem: "虫声新透绿窗纱"),
        Shichen(index: 2, branch: "寅", alias: "平旦", startHour: 3, poem: "处处闻啼鸟"),
        Shichen(index: 3, branch: "卯", alias: "日出", startHour: 5, poem: "晨光出照屋梁明"),
        Shichen(index: 4, branch: "辰", alias: "食时", startHour: 7, poem: "春风花草香"),
        Shichen(index: 5, branch: "巳", alias: "隅中", startHour: 9, poem: "松间沙路净无泥"),
        Shichen(index: 6, branch: "午", alias: "日中", startHour: 11, poem: "闲看儿童捉柳花"),
        Shichen(index: 7, branch: "未", alias: "日昳", startHour: 13, poem: "浮萍一道开"),
        Shichen(index: 8, branch: "申", alias: "晡时", startHour: 15, poem: "返景入深林"),
        Shichen(index: 9, branch: "酉", alias: "日入", startHour: 17, poem: "夕阳无限好"),
        Shichen(index: 10, branch: "戌", alias: "黄昏", startHour: 19, poem: "明月松间照"),
        Shichen(index: 11, branch: "亥", alias: "人定", startHour: 21, poem: "巴山夜雨涨秋池")
    ]

    static func read(hour: Int, minute: Int) -> TimeReading {
        precondition((0..<24).contains(hour) && (0..<60).contains(minute))
        let index = ((hour + 1) % 24) / 2
        let current = shichen[index]
        let elapsedMinutes = ((hour - current.startHour + 24) % 24) * 60 + minute
        return TimeReading(
            shichen: current,
            hour: hour,
            minute: minute,
            phase: elapsedMinutes < 60 ? "初" : "正",
            quarterNumber: (elapsedMinutes % 60) / 15 + 1
        )
    }

    static func read(date: Date) -> TimeReading {
        let parts = Calendar.current.dateComponents([.hour, .minute], from: date)
        return read(hour: parts.hour ?? 0, minute: parts.minute ?? 0)
    }

    static func preview(shichenIndex: Int, currentMinute: Int) -> TimeReading {
        precondition(shichen.indices.contains(shichenIndex), "shichenIndex must be in 0..<12")
        precondition((0..<60).contains(currentMinute), "currentMinute must be in 0..<60")
        return read(hour: shichen[shichenIndex].startHour, minute: currentMinute)
    }

    static func watch(hour: Int, minute: Int) -> WatchReading? {
        guard (0..<24).contains(hour), (0..<60).contains(minute) else { return nil }
        guard (hour >= 19 || hour < 5) else { return nil }
        let minutesSinceFirstWatch = hour >= 19
            ? (hour - 19) * 60 + minute
            : (hour + 5) * 60 + minute
        guard minutesSinceFirstWatch < 10 * 60 else { return nil }
        return WatchReading(gengNumber: minutesSinceFirstWatch / 120 + 1,
                            pointNumber: (minutesSinceFirstWatch % 120) / 24 + 1)
    }

    static func chineseNumber(_ number: Int) -> String {
        let numbers = Array("一二三四五六七八")
        precondition((1...numbers.count).contains(number))
        return String(numbers[number - 1])
    }
}
