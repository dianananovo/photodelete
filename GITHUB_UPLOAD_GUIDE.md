# 咻咻相册 · 上传 GitHub 并自动打包 APK 全流程指引

本项目已预先配置好了 GitHub Actions 自动化编译脚本（`.github/workflows/build.yml`）。
只要将代码上传到 GitHub，云端服务器就会**全自动完成 Android APK 编译打包**，无需在本地配置任何 Android SDK 或 JDK 环境。

---

## 第一步：在 GitHub 上新建仓库

1. 打开 [GitHub 官网](https://github.com) 并登录您的账号；
2. 点击右上角的 **`+`** 按钮，选择 **`New repository`**（新建仓库）；
3. 填写仓库信息：
   - **Repository name**（仓库名称）：例如输入 `photoclean` 或 `xiuxiu-photoclean`；
   - **Public / Private**：建议选择 **Private**（私有仓库，仅自己可见，保证隐私）；
   - **重要**：**不要**勾选 "Add a README file"、"Add .gitignore" 等初始化选项（因为本地项目中已全部为您创建好）；
4. 点击绿色的 **`Create repository`** 按钮。

---

## 第二步：将本地代码上传到 GitHub（两种方法任选其一）

### 方法 A：使用 GitHub Desktop 客户端（最简单，纯鼠标操作，强烈推荐）

1. 下载并安装官方客户端：[GitHub Desktop 官网](https://desktop.github.com/)；
2. 安装后登录您的 GitHub 账号；
3. 点击菜单栏 **`File`** ➔ **`Add local repository...`**；
4. 点击 **`Choose...`**，选中您的项目目录：`F:\photodelete`；
   *(如果提示该目录还不是 git 仓库，点击蓝色的 **"create a repository"** 确认即可)*；
5. 点击客户端左下角的 **`Commit to main`**（提交更改）；
6. 点击右上角的 **`Publish repository`** 按钮；
   - 勾选 `Keep this code private`（保持私有）；
   - 点击 **`Publish Repository`** 确认上传！

---

### 方法 B：使用 Git 命令行（适合有命令行基础的用户）

1. 下载安装 [Git for Windows](https://git-scm.com/download/win)；
2. 在项目根目录 `F:\photodelete` 页面空白处右键，选择 **“在终端中打开”** 或 **“Git Bash Here”**；
3. 依次复制并运行以下命令（将其中的链接替换为您在第一步中创建的仓库地址）：

```bash
# 1. 初始化本地仓库
git init

# 2. 添加所有代码文件
git add .

# 3. 提交到本地
git commit -m "feat: 初代经典日系微拟物纯粉纯蓝版相册清理App"

# 4. 设置默认分支为 main
git branch -M main

# 5. 关联您在 GitHub 新建的仓库地址 (将下行替换为您自己的仓库地址)
git remote add origin https://github.com/您的用户名/您的仓库名.git

# 6. 推送代码至 GitHub
git push -u origin main
```

---

## 第三步：在 GitHub Actions 中下载打好的 APK

1. 代码上传成功后，在浏览器中打开您的 GitHub 仓库页面；
2. 点击仓库顶部的 **`Actions`** 选项卡；
3. 您会看到一个正在自动运行的工作流：
   - 名称：**“编译生成一加15相册清理APK”**
   - 此时左侧会有一个黄色小圆圈在旋转，表示云端正在自动分配 JDK 17、下载依赖并编译代码；
4. **等待约 2~3 分钟**，圆圈变成**绿色对勾 ✔️** 即表示编译成功；
5. 点击进入该构建记录，页面滚动到最下方的 **Artifacts（生成物）** 区域；
6. 直接点击 **`XiuXiuPhotoClean-v1.0-Debug-APK`** 下载压缩包；
7. 解压后即可得到 **`app-debug.apk`**！

---

## 第四步：安装到一加 15 手机

1. 将 `app-debug.apk` 通过 USB 数据线、微信传输助手或 QQ 发送到手机；
2. 在手机文件管理器中点击 `app-debug.apk` 进行安装；
3. 首次安装如提示“未知来源应用”，允许本次安装即可；
4. 打开 App，授予相册读取与修改权限，即可享受流畅的单手滑卡照片清理！
