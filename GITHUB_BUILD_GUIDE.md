# SleepRecorder Android - GitHub Actions 自动构建

## 🎯 最简单的 APK 获取方式

我已经为你配置好了 **GitHub Actions**，只要按下面步骤操作，就能自动编译出 APK：

---

## 方法一：用我创建的仓库（最简单）

### 步骤 1：告诉我你的 GitHub 用户名

我可以用我的账号创建仓库，然后把构建好的 APK 链接发给你下载。

**或者你可以自己创建：**

### 步骤 2：自己创建 GitHub 仓库

1. 打开 https://github.com/new
2. 填写：
   - Repository name: `SleepRecorder-Android`
   - Description: `睡眠监测 App`
   - 勾选 "Add a README file"
   - 选择 "Public"
3. 点击 **Create repository**

### 步骤 3：上传代码

#### 如果你会用 Git：
```bash
cd SleepRecorderAndroid
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR用户名/SleepRecorder-Android.git
git push -u origin main
```

#### 如果你不会用 Git（网页上传）：
1. 在 GitHub 仓库页面，点击 "Add file" → "Upload files"
2. 把 `SleepRecorderAndroid` 文件夹里的所有内容打包成 zip
3. 上传 zip 文件
4. 点击 "Commit changes"

### 步骤 4：触发构建

1. 在 GitHub 页面，点击 **Actions** 标签
2. 你会看到 "Build APK" 工作流
3. 点击它，然后点击 **Run workflow** → **Run workflow**
4. 等待 5-10 分钟（绿色勾表示完成）

### 步骤 5：下载 APK

1. 点击完成的 workflow（绿色勾那个）
2. 页面下方找到 **Artifacts** 部分
3. 点击 **apk-release** 下载
4. 解压下载的文件，得到 `app-release.apk`

### 步骤 6：安装到手机

1. 把 APK 传到手机（微信文件传输、QQ、数据线都行）
2. 在手机上打开 APK 文件
3. 系统会提示"允许安装未知来源应用" → 点击 **设置** → 允许
4. 返回，再次点击安装

---

## 方法二：找朋友帮忙（最快）

如果你有朋友用 Android Studio，直接把代码发给他：
1. 打包 `SleepRecorderAndroid` 文件夹发给他
2. 他打开 Android Studio → Open → 选择文件夹
3. 点击 Build → Build Bundle(s) / APK(s) → Build APK(s)
4. 把生成的 APK 发给你

**整个过程 5 分钟搞定**

---

## 方法三：用在线构建服务

如果不想用 GitHub，还有一些在线 APK 构建服务（搜索 "online android apk builder"），但可能收费或有限制。

---

## ❓ 遇到问题？

告诉我具体情况：
- 卡在哪一步？
- 有什么错误提示？
- 你能用 Git 吗？

我可以根据你的情况调整方案。