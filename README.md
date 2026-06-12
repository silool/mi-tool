# 小米快捷

按一下切换小米手机设置：日常模式 / 游戏模式。

## 功能

| | 日常 | 游戏 |
|------|:--:|:--:|
| 省电模式 | ✅ 开 | ❌ 关 |
| 刷新率 | 120Hz | 120Hz |
| 护眼模式 | ✅ 开 | ❌ 关 |

## 首次使用

1. 安装 APK 后打开，系统会弹出"修改系统设置"请求 → 点**允许**
2. 省电模式需要额外权限，用数据线连电脑执行一次：
   ```bash
   adb shell pm grant com.mitool android.permission.WRITE_SECURE_SETTINGS
   ```
   如果不想连电脑，省电模式会跳转到系统设置页手动开关

## 构建 APK

### 本地
用 Android Studio 打开 `mi-tool/` 目录 → Build → Build APK

### GitHub Actions（推荐）
推送后在 Actions 页面自动构建，下载 APK
