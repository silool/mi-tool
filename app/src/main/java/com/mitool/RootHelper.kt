package com.mitool

import java.io.BufferedReader
import java.io.InputStreamReader

object RootHelper {

    private var available: Boolean? = null

    fun isRootAvailable(): Boolean {
        if (available == null) {
            available = try {
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                p.waitFor()
                p.exitValue() == 0
            } catch (e: Exception) {
                false
            }
        }
        return available!!
    }

    /**
     * 以 root 权限执行一条命令，返回 stdout
     */
    fun exec(vararg commands: String): String {
        val sb = StringBuilder()
        try {
            val cmd = commands.joinToString(" && ")
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val errReader = BufferedReader(InputStreamReader(p.errorStream))
            reader.forEachLine { sb.appendLine(it) }
            errReader.forEachLine { sb.appendLine("[E] $it") }
            p.waitFor()
            reader.close()
            errReader.close()
        } catch (e: Exception) {
            sb.appendLine("Error: ${e.message}")
        }
        return sb.toString()
    }

    // ===== 系统设置命令 =====

    /** 省电模式 — 尝试多个小米/安卓键 */
    fun setBatterySaver(enable: Boolean) {
        val v = if (enable) 1 else 0
        exec(
            "settings put system power_save_mode_open $v",
            "settings put system power_saver_mode $v",
            "settings put system battery_saver_mode $v",
            "settings put global low_power $v"
        )
    }

    /** 刷新率 */
    fun setRefreshRate(hz: Int) {
        exec(
            "settings put system peak_refresh_rate ${hz}.0",
            "settings put system user_refresh_rate $hz"
        )
    }

    /** 护眼/阅读模式 */
    fun setReadingMode(enable: Boolean) {
        val v = if (enable) 1 else 0
        exec(
            "settings put system screen_paper_mode_enabled $v",
            "settings put system reading_mode_status $v"
        )
    }
}
