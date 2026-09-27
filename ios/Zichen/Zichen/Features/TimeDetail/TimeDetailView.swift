import SwiftUI

struct TimeDetailView: View {
    let index: Int
    private var shichen: Shichen { TraditionalTime.shichen[index] }
    private var content: TimeDetailContent { TimeDetailContents.forShichen(index: index) }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 22) {
                VStack(alignment: .leading, spacing: 8) {
                    Eyebrow(text: "十二时辰 · \(content.zodiac)生肖")
                    HStack(alignment: .firstTextBaseline, spacing: 12) {
                        Text(shichen.branch).font(AppTheme.serif(44)).foregroundStyle(AppTheme.goldLight)
                        Text(shichen.alias).font(AppTheme.serif(20)).foregroundStyle(AppTheme.text)
                        Spacer()
                        Text(shichen.interval).font(AppTheme.data(12)).foregroundStyle(AppTheme.muted)
                    }
                    Text(content.impression).font(AppTheme.serif(15)).foregroundStyle(AppTheme.muted)
                }
                QuietRule()
                VStack(alignment: .leading, spacing: 10) {
                    Text(content.poemTitle).font(AppTheme.serif(20, weight: .medium)).foregroundStyle(AppTheme.text)
                    Text(content.poet).font(AppTheme.serif(13)).foregroundStyle(AppTheme.gold)
                    ForEach(Array(content.poemLines.enumerated()), id: \.offset) { item in
                        Text(item.element).font(AppTheme.serif(16)).lineSpacing(7).foregroundStyle(AppTheme.text)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                QuietRule()
                Text("出处  \(content.source)").font(AppTheme.serif(12)).foregroundStyle(AppTheme.muted)
            }
            .padding(.horizontal, 24)
            .padding(.top, 24)
            .padding(.bottom, 38)
        }
        .scrollIndicators(.hidden)
        .inkBackground()
    }
}
