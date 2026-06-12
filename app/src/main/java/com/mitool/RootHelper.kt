package com.mitool

import java.io.BufferedReader
import java.io.InputStreamReader

object RootHelper {

    private var available: Boolean? = null

    fun isRootAvailable(): Boolean {
        if (available == null) {
            available = try {
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                p.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)
                p.exitValue() == 0
            } catch (e: Exception) { false }
        }
        return available!!
    }

    fun exec(vararg commands: String): String {
        val sb = StringBuilder()
        try {
            val cmd = commands.joinToString(" && ")
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val errReader = BufferedReader(InputStreamReader(p.errorStream))
            reader.forEachLine { sb.appendLine(it) }
            errReader.forEachLine { sb.appendLine("[E] $it") }
            p.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)
            reader.close(); errReader.close()
        } catch (e: Exception) { sb.appendLine("Error: ${e.message}") }
        return sb.toString()
    }

    // ===== 省电模式 — 多路径尝试 =====
    fun setBatterySaver(enable: Boolean) {
        val v = if (enable) 1 else 0
        exec(
            // 原生 Android 键
            "settings put global low_power $v",
            // 小米 System 键
            "settings put system power_save_mode_open $v",
            "settings put system power_saver_mode $v",
            "settings put system battery_saver_mode $v",
            "settings put system smart_power_save $v",
            // 小米 Secure 键
            "settings put secure low_power_activation $v",
            // cmd power（Android 9+）
            "cmd power set-mode ${if (enable) 1 else 0}",
            // 触发省电广播
            "am broadcast -a android.os.action.POWER_SAVE_MODE_CHANGED --ez mode $enable"
        )
    }

    // ===== 刷新率 =====
    fun setRefreshRate(hz: Int) {
        exec(
            "settings put system peak_refresh_rate ${hz}.0",
            "settings put system user_refresh_rate $hz",
            "settings put system min_refresh_rate $hz"
        )
    }

    // ===== 护眼 =====
    fun setReadingMode(enable: Boolean) {
        val v = if (enable) 1 else 0
        exec(
            "settings put system screen_paper_mode_enabled $v",
            "settings put system reading_mode_status $v",
            "settings put system display_paper_mode $v"
        )
    }
}
