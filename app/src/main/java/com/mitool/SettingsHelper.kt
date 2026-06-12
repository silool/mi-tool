package com.mitool

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast

class SettingsHelper(private val ctx: Context) {

    companion object {
        private const val TAG = "MITool"
        private const val KEY_PEAK_REFRESH = "peak_refresh_rate"
        private const val KEY_USER_REFRESH = "user_refresh_rate"
        private const val KEY_READING_MODE = "screen_paper_mode_enabled"
        private const val KEY_READING_MODE_ALT = "reading_mode_status"
        private const val KEY_LOW_POWER = "low_power"
    }

    /** 日常模式 */
    fun setDailyMode() = applyMode(batterySaver = true, refreshHz = 120, readingMode = true)

    /** 游戏模式 */
    fun setGameMode() = applyMode(batterySaver = false, refreshHz = 120, readingMode = false)

    /**
     * 自动选择激活方案：
     * 1. 有 Root → 直接 su 执行
     * 2. 无 Root → Settings API（需 WRITE_SETTINGS）
     */
    private fun applyMode(batterySaver: Boolean, refreshHz: Int, readingMode: Boolean) {
        if (RootHelper.isRootAvailable()) {
            Log.d(TAG, "✅ 使用 Root 方案")
            RootHelper.setBatterySaver(batterySaver)
            RootHelper.setRefreshRate(refreshHz)
            RootHelper.setReadingMode(readingMode)
        } else {
            Log.d(TAG, "⚠️ 使用 Settings API 方案")
            setBatterySaverAPI(batterySaver)
            setRefreshRateAPI(refreshHz)
            setReadingModeAPI(readingMode)
        }
    }

    // ===== Settings API 方案 =====

    private fun setBatterySaverAPI(enable: Boolean) {
        try {
            Settings.Global.putInt(ctx.contentResolver, KEY_LOW_POWER, if (enable) 1 else 0)
        } catch (e: SecurityException) {
            Log.w(TAG, "无 WRITE_SECURE_SETTINGS 权限")
            Toast.makeText(ctx, "省电模式需 ADB 授权或 Root", Toast.LENGTH_SHORT).show()
            openBatterySettings()
        }
    }

    private fun setRefreshRateAPI(hz: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.System.putFloat(ctx.contentResolver, KEY_PEAK_REFRESH, hz.toFloat())
            }
            Settings.System.putInt(ctx.contentResolver, KEY_USER_REFRESH, hz)
        } catch (e: Exception) {
            Log.e(TAG, "刷新率失败: ${e.message}")
            openDisplaySettings()
        }
    }

    private fun setReadingModeAPI(enable: Boolean) {
        val v = if (enable) 1 else 0
        try { Settings.System.putInt(ctx.contentResolver, KEY_READING_MODE, v) } catch (_: Exception) {}
        try { Settings.System.putInt(ctx.contentResolver, KEY_READING_MODE_ALT, v) } catch (_: Exception) {}
    }

    // ===== 跳转系统设置 =====

    private fun openBatterySettings() {
        try {
            ctx.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {}
    }

    private fun openDisplaySettings() {
        try {
            ctx.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {}
    }
}
