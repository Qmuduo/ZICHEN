import SwiftUI

struct AlmanacDetailView: View {
    let day: LunarCalendarDay

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 6) {
                    Eyebrow(text: "日期笺")
                    Text(day.gregorianDateText).font(AppTheme.data(14)).foregroundStyle(AppTheme.text)
                    Text(day.weekday).font(AppTheme.serif(12)).foregroundStyle(AppTheme.muted)
                    Text(day.lunarDateText.map { "农历\($0)" } ?? "农历资料暂不可用")
                        .font(AppTheme.serif(25)).foregroundStyle(AppTheme.goldLight)
                    if let caption = day.lunarYearCaption {
                        Text(caption).font(AppTheme.serif(13)).foregroundStyle(AppTheme.muted)
                    }
                    if !day.eventLabels.isEmpty {
                        Text(day.eventLabels.joined(separator: " · ")).font(AppTheme.serif(13)).foregroundStyle(AppTheme.gold)
                    }
                    Text("\(day.yearGanzhi ?? "暂无资料")年 · \(day.monthGanzhi ?? "暂无资料")月 · \(day.dayGanzhi ?? "暂无资料")日")
                        .font(AppTheme.serif(12)).foregroundStyle(AppTheme.muted)
                }
                QuietRule()
                if day.hasFullAlmanac {
                    field("纳音", day.nayin)
                    field("冲煞", [day.clash, day.shaDirection.map { "煞\($0)" }].compactMap { $0 }.joined(separator: "　"))
                    field("彭祖百忌", [day.pengzuGan, day.pengzuZhi].compactMap { $0 }.joined(separator: "\n"))
                    HStack(spacing: 4) {
                        direction("喜神", day.xiDirection)
                        direction("福神", day.fuDirection)
                        direction("财神", day.caiDirection)
                    }
                    QuietRule()
                    field("宜", day.yi.joined(separator: "　"), color: AppTheme.preview)
                    field("忌", day.ji.joined(separator: "　"), color: AppTheme.warning)
                    field("吉神", day.auspiciousDeities.joined(separator: "　"), color: AppTheme.gold)
                    field("凶神", day.inauspiciousDeities.joined(separator: "　"), color: AppTheme.warning)
                } else {
                    Text("该年份未包含完整民俗资料")
                        .font(AppTheme.serif(15)).foregroundStyle(AppTheme.text)
                    Text("完整黄历资料覆盖 2025—2028 年。\n\(!day.hasLunarData ? "该日期暂无可靠农历换算资料。" : "其他年份仅显示历法引擎可可靠提供的基础信息。")")
                        .font(AppTheme.serif(12)).lineSpacing(5).foregroundStyle(AppTheme.muted)
                }
                QuietRule()
                Text("传统民俗资料，仅供参考")
                    .font(AppTheme.serif(12)).foregroundStyle(AppTheme.muted)
                Text(day.sourceDescription)
                    .font(AppTheme.serif(10)).lineSpacing(4).foregroundStyle(AppTheme.muted.opacity(0.85))
            }
            .padding(.horizontal, 20).padding(.top, 18).padding(.bottom, 32)
        }
        .scrollIndicators(.hidden)
        .inkBackground()
    }

    private func field(_ title: String, _ value: String?, color: Color = AppTheme.text) -> some View {
        VStack(spacing: 8) {
            HStack(alignment: .top, spacing: 12) {
                Text(title).font(AppTheme.serif(12)).foregroundStyle(color).frame(width: 68, alignment: .leading)
                Text((value?.isEmpty == false ? value : nil) ?? "暂无资料")
                    .font(AppTheme.serif(12)).foregroundStyle(AppTheme.text).frame(maxWidth: .infinity, alignment: .leading)
                    .fixedSize(horizontal: false, vertical: true)
            }
            QuietRule()
        }
        .accessibilityElement(children: .combine)
    }

    private func direction(_ title: String, _ value: String?) -> some View {
        VStack(spacing: 6) {
            Text(title).font(AppTheme.serif(11)).foregroundStyle(AppTheme.muted)
            Text(value ?? "暂无资料").font(AppTheme.serif(12)).foregroundStyle(AppTheme.text)
                .lineLimit(1).minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity).padding(.vertical, 10)
        .background(AppTheme.surface, in: RoundedRectangle(cornerRadius: 9))
        .accessibilityElement(children: .combine)
    }
}
