import SwiftUI

enum AppTheme {
    static let ink = Color(red: 15.0 / 255, green: 14.0 / 255, blue: 13.0 / 255)
    static let surface = Color(red: 25.0 / 255, green: 23.0 / 255, blue: 20.0 / 255)
    static let gold = Color(red: 217.0 / 255, green: 169.0 / 255, blue: 75.0 / 255)
    static let goldLight = Color(red: 236.0 / 255, green: 207.0 / 255, blue: 143.0 / 255)
    static let preview = Color(red: 127.0 / 255, green: 191.0 / 255, blue: 164.0 / 255)
    static let text = Color(red: 207.0 / 255, green: 201.0 / 255, blue: 187.0 / 255)
    static let muted = Color(red: 142.0 / 255, green: 136.0 / 255, blue: 121.0 / 255)
    static let rule = Color(red: 94.0 / 255, green: 90.0 / 255, blue: 83.0 / 255)
    static let warning = Color(red: 201.0 / 255, green: 111.0 / 255, blue: 103.0 / 255)

    static func serif(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("Songti SC", size: size, relativeTo: .body).weight(weight)
    }

    static func data(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("Menlo", size: size, relativeTo: .body).weight(weight)
    }
}

struct InkBackground: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(AppTheme.ink.ignoresSafeArea())
            .foregroundStyle(AppTheme.text)
            .preferredColorScheme(.dark)
    }
}

extension View {
    func inkBackground() -> some View { modifier(InkBackground()) }
}

struct QuietRule: View {
    var body: some View { Rectangle().fill(AppTheme.rule.opacity(0.42)).frame(height: 1) }
}

struct Eyebrow: View {
    let text: String
    var body: some View {
        Text(text)
            .font(AppTheme.data(10, weight: .medium))
            .tracking(2.1)
            .textCase(.uppercase)
            .foregroundStyle(AppTheme.muted)
    }
}
