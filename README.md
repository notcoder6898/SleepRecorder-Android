# SleepRecorder Android

## 🎉 好消息：Android 可以安装 APK！

这是睡眠监测 App 的 Android 版本，功能与 iOS 版本相同。

---

## 📦 获取 APK 的 3 种方式

### 方式 1：GitHub Actions 自动构建（推荐，零成本）

我已经为你准备了 GitHub Actions 配置，可以直接在 GitHub 上编译出 APK：

1. **把代码上传到 GitHub**（我可以帮你创建仓库）
2. **GitHub 自动编译**，生成 APK
3. **直接下载 APK 安装**

完全免费，不需要你的电脑！

### 方式 2：找朋友帮忙打包（最快）

找有 Android Studio 的朋友：
1. 把代码发给他
2. 他点击 Build → Build APK
3. 把 APK 发给你安装

**成本：¥0，时间：10 分钟**

### 方式 3：自己安装 Android Studio（想学的话）

比 Xcode 简单很多，Windows 电脑就能用：
1. 下载 Android Studio（免费）
2. 打开这个项目
3. Build → Build APK

---

## 🚀 快速开始（方式 1：GitHub Actions）

### 第 1 步：创建 GitHub 仓库

1. 访问 https://github.com/new
2. 仓库名填 `SleepRecorder-Android`
3. 选择 "Public"（公开，免费）
4. 点击 "Create repository"

### 第 2 步：上传代码

```bash
# 在你电脑上下载 Git（如果没有）
# 然后运行：

cd SleepRecorderAndroid
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/SleepRecorder-Android.git
git push -u origin main
```

### 第 3 步：触发构建

1. 在 GitHub 页面，点击 "Actions" 标签
2. 点击 "Build APK" 工作流
3. 点击 "Run workflow"
4. 等待 5-10 分钟

### 第 4 步：下载 APK

1. 构建完成后，点击最新的 workflow run
2. 在 "Artifacts" 部分下载 `apk-release`
3. 解压得到 `app-release.apk`

### 第 5 步：安装到手机

1. 把 APK 传到手机（微信、QQ、邮件都行）
2. 手机上点击 APK 文件
3. 允许"安装未知来源应用"
4. 安装完成！

---

## 📱 项目结构

```
SleepRecorderAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/sleeprecorder/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── SleepRecorderApp.kt
│   │   │   ├── data/           # 数据库
│   │   │   ├── service/        # 录音服务
│   │   │   ├── ui/             # 界面
│   │   │   └── viewmodel/      # 数据管理
│   │   ├── res/                # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── build.gradle.kts
```

---

## ✨ 功能特点

- ✅ **有声片段录制**：只录有声音的部分
- ✅ **智能闹钟**：声音触发唤醒
- ✅ **深色主题**：夜间不刺眼
- ✅ **本地存储**：Room 数据库
- ✅ **NAS 备份**：预留接口
- ✅ **省电优化**：16kHz 采样率

---

## ⚠️ 注意事项

1. **安装前**：需要在手机设置里允许"安装未知来源应用"
2. **权限**：首次打开需要授权录音和通知权限
3. **电池优化**：建议将 App 加入电池优化白名单，防止被系统杀死

---

## 🆘 需要帮助？

如果 GitHub Actions 方式搞不定，告诉我，我可以：
1. 把代码上传到 **我的 GitHub**，直接给你 APK 下载链接
2. 详细指导 Android Studio 安装

---

Made with ❤️ for better sleep