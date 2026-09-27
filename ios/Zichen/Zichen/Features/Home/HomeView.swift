import SwiftUI

struct HomeView: View {
    let open: (AppRoute) -> Void
    @State private var previewedIndex: Int?

    var body: some View {
        TimelineView(.periodic(from: .now, by: 1)) { timeline in
            let now = timeline.date
            let actual = TraditionalTime.read(date: now)
            let reading = previewedIndex.map { TraditionalTime.preview(shichenIndex: $0, currentMinute: actual.minute) } ?? actual
            let watch = TraditionalTime.watch(hour: actual.hour, minute: actual.minute)

            ScrollView {
                VStack(spacing: 20) {
                    header(reading: actual)
                    ShichenDialView(now: now, previewedIndex: previewedIndex) { previewedIndex = $0 }
                        .frame(height: 320)
                        .padding(.horizontal, 8)
                    VStack(spacing: 9) {
                        HStack(alignment: .firstTextBaseline, spacing: 10) {
                            Text(reading.shichen.branch)
                                .font(AppTheme.serif(35))
                                .foregroundStyle(previewedIndex == nil ? AppTheme.goldLight : AppTheme.preview)
                            VStack(alignment: .leading, spacing: 3) {
                                Text("\(reading.shichen.alias) · \(reading.quarterLabel)")
                                    .font(AppTheme.serif(19))
                                    .foregroundStyle(AppTheme.text)
                                Text("\(reading.shichen.interval)  ·  \(reading.clockText)")
                                    .font(AppTheme.data(12))
                                    .foregroundStyle(AppTheme.muted)
                            }
                            Spacer(minLength: 0)
                        }
                        Text("「\(reading.shichen.poem)」")
                            .font(AppTheme.serif(16))
                            .foregroundStyle(AppTheme.text.opacity(0.88))
                            .frame(maxWidth: .infinity, alignment: .leading)
                        if let watch {
                            Text(watch.label)
                                .font(AppTheme.serif(12))
                                .foregroundStyle(AppTheme.gold.opacity(0.86))
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        if previewedIndex != nil {
                            Button("回到此刻") { previewedIndex = nil }
                                .font(AppTheme.serif(13))
                                .foregroundStyle(AppTheme.preview)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.top, 2)
                        }
                        Button { open(.timeDetail(reading.shichen.index)) } label: {
                            HStack(spacing: 7) {
                                Text("读这段时辰笺")
                                Image(systemName: "arrow.up.right").font(.system(size: 9))
                            }
                            .font(AppTheme.serif(12))
                            .foregroundStyle(AppTheme.gold)
                            .padding(.top, 4)
                        }
                        .buttonStyle(.plain)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(.horizontal, 26)

                    QuietRule().padding(.horizontal, 26)
                    VStack(spacing: 4) {
                        entry("月历", caption: "农历与节令", symbol: "moon.stars") { open(.lunarCalendar) }
                        entry("钟历", caption: "把时辰放进日期", symbol: "sun.max") { open(.clockCalendar) }
                        entry("仪式计时", caption: "一刻 · 一炷香 · 半个时辰", symbol: "hourglass") { open(.timer) }
                    }
                    .padding(.horizontal, 22)
                    Text(LocalGregorianCalendar.dateText(now, includesWeekday: true))
                        .font(AppTheme.data(11))
                        .foregroundStyle(AppTheme.muted.opacity(0.8))
                        .padding(.top, 5)
                }
                .padding(.top, 16)
                .padding(.bottom, 28)
            }
            .scrollIndicators(.hidden)
            .accessibilityElement(children: .contain)
        }
    }

    private func header(reading: TimeReading) -> some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 5) {
                Eyebrow(text: "古时一日")
                Text("十二时辰")
                    .font(AppTheme.serif(26, weight: .medium))
                    .foregroundStyle(AppTheme.goldLight)
            }
            Spacer()
            Text(reading.clockText)
                .font(AppTheme.data(16, weight: .medium))
                .foregroundStyle(AppTheme.text)
                .monospacedDigit()
                .accessibilityLabel("现在时间 \(reading.clockText)")
        }
        .padding(.horizontal, 26)
    }

    private func entry(_ title: String, caption: String, symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: symbol)
                    .font(.system(size: 15, weight: .regular))
                    .foregroundStyle(AppTheme.gold.opacity(0.9))
                    .frame(width: 23)
                VStack(alignment: .leading, spacing: 3) {
                    Text(title).font(AppTheme.serif(15)).foregroundStyle(AppTheme.text)
                    Text(caption).font(AppTheme.serif(11)).foregroundStyle(AppTheme.muted)
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(AppTheme.muted.opacity(0.8))
            }
            .padding(.vertical, 10)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityHint("打开\(title)")
    }
}
