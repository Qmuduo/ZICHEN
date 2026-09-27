import Foundation

struct TimeDetailContent: Equatable {
    let zodiac: String
    let impression: String
    let poet: String
    let poemTitle: String
    let poemLines: [String]
    let source: String
}

enum TimeDetailContents {
    private static let entries: [TimeDetailContent] = [
        .init(zodiac: "鼠", impression: "夜色沉静，万籁俱寂。", poet: "苏轼 · 北宋", poemTitle: "临江仙 · 夜归临皋", poemLines: ["夜饮东坡醒复醉，归来仿佛三更。", "家童鼻息已雷鸣，敲门都不应，倚杖听江声。", "长恨此身非我有，何时忘却营营？", "夜阑风静縠纹平。小舟从此逝，江海寄余生。"], source: "《东坡词》"),
        .init(zodiac: "牛", impression: "晨鸡将啼，夜色尚未褪尽。", poet: "刘方平 · 唐", poemTitle: "夜月", poemLines: ["更深月色半人家，北斗阑干南斗斜。", "今夜偏知春气暖，虫声新透绿窗纱。"], source: "《全唐诗》卷二五一"),
        .init(zodiac: "虎", impression: "天色欲晓，清气初生。", poet: "孟浩然 · 唐", poemTitle: "春晓", poemLines: ["春眠不觉晓，处处闻啼鸟。", "夜来风雨声，花落知多少。"], source: "《全唐诗》卷一六〇"),
        .init(zodiac: "兔", impression: "旭日初升，草木渐渐承光。", poet: "白居易 · 唐", poemTitle: "早兴", poemLines: ["晨光出照屋梁明，初打开门鼓一声。", "犬上阶眠知地湿，鸟临窗语报天晴。", "半销宿酒头仍重，新脱冬衣体乍轻。", "睡觉心空思想尽，近来乡梦不多成。"], source: "《全唐诗》卷四四三"),
        .init(zodiac: "龙", impression: "朝食之时，日色明朗起来。", poet: "杜甫 · 唐", poemTitle: "绝句二首 · 其一", poemLines: ["迟日江山丽，春风花草香。", "泥融飞燕子，沙暖睡鸳鸯。"], source: "《全唐诗》卷二二八"),
        .init(zodiac: "蛇", impression: "日光明净，庭院里已有暖意。", poet: "苏轼 · 北宋", poemTitle: "浣溪沙 · 游蕲水清泉寺", poemLines: ["山下兰芽短浸溪，松间沙路净无泥，萧萧暮雨子规啼。", "谁道人生无再少？门前流水尚能西，休将白发唱黄鸡。"], source: "《东坡乐府》"),
        .init(zodiac: "马", impression: "日当正中，万物明朗。", poet: "杨万里 · 南宋", poemTitle: "闲居初夏午睡起二绝句 · 其一", poemLines: ["梅子留酸软齿牙，芭蕉分绿与窗纱。", "日长睡起无情思，闲看儿童捉柳花。"], source: "《诚斋集》"),
        .init(zodiac: "羊", impression: "日影西斜，午后渐入清闲。", poet: "白居易 · 唐", poemTitle: "池上二绝 · 其二", poemLines: ["小娃撑小艇，偷采白莲回。", "不解藏踪迹，浮萍一道开。"], source: "《全唐诗》卷四五五"),
        .init(zodiac: "猴", impression: "日影渐长，山林间已有斜光。", poet: "王维 · 唐", poemTitle: "鹿柴", poemLines: ["空山不见人，但闻人语响。", "返景入深林，复照青苔上。"], source: "《全唐诗》卷一二八"),
        .init(zodiac: "鸡", impression: "夕阳向晚，暮色渐次合拢。", poet: "李商隐 · 唐", poemTitle: "乐游原", poemLines: ["向晚意不适，驱车登古原。", "夕阳无限好，只是近黄昏。"], source: "《李义山诗集》"),
        .init(zodiac: "狗", impression: "灯火初上，山月照入松间。", poet: "王维 · 唐", poemTitle: "山居秋暝", poemLines: ["空山新雨后，天气晚来秋。", "明月松间照，清泉石上流。", "竹喧归浣女，莲动下渔舟。", "随意春芳歇，王孙自可留。"], source: "《全唐诗》卷一二六"),
        .init(zodiac: "猪", impression: "人声已息，夜深渐入安眠。", poet: "李商隐 · 唐", poemTitle: "夜雨寄北", poemLines: ["君问归期未有期，巴山夜雨涨秋池。", "何当共剪西窗烛，却话巴山夜雨时。"], source: "《李义山诗集》卷第六")
    ]

    static func forShichen(index: Int) -> TimeDetailContent {
        precondition(entries.indices.contains(index), "shichen index must be in 0..<12")
        return entries[index]
    }
}
