# 时辰 iOS

原生 SwiftUI 版，最低支持 iOS 16，离线运行。

## 打开与运行

1. 在 macOS 上用 Xcode 打开 `Zichen.xcodeproj`。
2. 等待 Swift Package Manager 获取固定版本的 LunarSwift 1.1.8。
3. 选择 `Zichen` scheme 和 iPhone 模拟器，运行或执行 `ZichenTests`。
4. 连接 iPhone 后选择设备并运行；首次真机安装需在 Xcode 配置 Apple 开发者签名。

```sh
xcodebuild test -project Zichen.xcodeproj -scheme Zichen -destination 'platform=iOS Simulator,name=iPhone 16'
```

Windows 环境可编辑源码，但不能执行 Xcode 构建、iOS 模拟器、签名或真机安装。提交前这些项目需在 macOS/Xcode 环境完成。

## 功能

- 十二时辰盘、实时读数、时辰预览和诗笺详情。
- 农历月历、钟历、共享月份/日期选择和黄历详情。
- 三档仪式计时、本机状态恢复、系统本地通知与可选钟磬音。

日历民俗字段只在 2025—2028 年完整显示；其他年份只显示历法引擎可可靠提供的日期信息。详见 `THIRD_PARTY_NOTICES.md`。
