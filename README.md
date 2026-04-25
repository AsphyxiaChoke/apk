# 打码器 Android 版

由 `打码器_PC版.py` (Tkinter) 移植到 Android (Kotlin + Jetpack Compose)。

## 工程结构

```
DaMaQi_Android/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/values/
        │   ├── strings.xml
        │   └── themes.xml
        └── java/com/example/damaqi/
            ├── Crypto.kt        # TEA 加密算法（与 PC 版一致）
            └── MainActivity.kt  # 三标签页 Compose UI
```

## 构建步骤

最低 Android 版本：**Android 7.0 (API 24)**。

### 方式一：GitHub Actions 云构建（无需本地环境）

仓库已配置 `.github/workflows/build.yml`，推上 GitHub 后会自动打包 APK。

```bash
cd DaMaQi_Android
git init -b main
git add .
git commit -m "init: DaMaQi Android"

# 在 GitHub 上新建一个空仓库（不要勾选 README/gitignore），拿到地址后：
git remote add origin https://github.com/<你的用户名>/<仓库名>.git
git push -u origin main
```

然后到仓库 **Actions** 页面，等绿色对勾出现，点进任务，
在底部 **Artifacts** 区域下载 `DaMaQi-debug-apk.zip`，解压即得 `app-debug.apk`。

> 不想 push？也可以在 Actions 页面手动点 **Run workflow** (workflow_dispatch)。

### 方式二：本地 Android Studio 打包

1. 打开 **Android Studio**（Hedgehog / Iguana 或更新版本），**Open** 工程目录。
2. 等 Gradle 同步完成。
3. 菜单 **Build → Build APK(s)**，或运行 `./gradlew assembleDebug`。
4. 输出位置：`app/build/outputs/apk/debug/app-debug.apk`。

## 与 PC 版的算法一致性

`Crypto.kt` 中的 `c51Encrypt32` 与 `encrypt32CustomDelta` 与 Python 版逐位等价，
通过 `Long` + `0xFFFFFFFFL`/`0xFFFFL` 掩码模拟 32/16 位无符号整数运算。
对于相同输入会产出相同密码。

## 三个功能标签

| 标签 | 输入 | 备注 |
| --- | --- | --- |
| 查账打码 | 线号 / 机号 / 序列号 / 前期盈利 / 当期盈利 / 允许次数 | 允许次数 0–99，超过自动截断为 99 |
| 参数/机芯打码 | 线号 / 机号 / 序列号 | 参数与机芯打码相同 |
| 后台打码 | 机号 | 机号上限 99999999；输出 `signature(10位) + random(5位)` |
