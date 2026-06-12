package com.mitool

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 无障碍服务 — Root 之外的备选方案。
 * 开启后自动接管省电模式的点击操作。
 * 「设置 → 无障碍 → 已安装的应用 → 小米快捷 → 开启」
 */
class MiAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MiAccessibilityService? = null
            private set
        private const val TAG = "MIToolAccess"
        private var pendingBatteryAction: Boolean? = null // true=开省电, false=关省电
    }

    private val helper by lazy { SettingsHelper(this) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return

        // 检测电池设置页面打开
        if (pendingBatteryAction != null &&
            (pkg == "com.android.settings" || pkg == "com.xiaomi.powerchecker" ||
             pkg == "com.miui.securitycenter" || pkg == "com.miui.powerkeeper")) {

            val root = rootInActiveWindow ?: return
            findAndClickBatteryToggle(root)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * 供 SettingsHelper 调用：启动省电模式的自动点击
     */
    fun requestBatterySaver(enable: Boolean) {
        pendingBatteryAction = enable
        // 打开小米省电设置页
        val intent = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // 尝试小米省电设置
            setClassName("com.android.settings", "com.android.settings.Settings\$BatterySaverSettingsActivity")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // 回退到通用电池设置
            try {
                startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (_: Exception) {}
        }
    }

    private fun findAndClickBatteryToggle(root: AccessibilityNodeInfo) {
        val target = pendingBatteryAction ?: return

        // 搜索节点文本包含"省电"的 Switch
        val switches = root.findAccessibilityNodeInfosByViewId("android:id/switch_widget")
        if (switches.isNullOrEmpty()) {
            // 尝试通过文本查找
            val nodes = root.findAccessibilityNodeInfosByText("省电")
            for (node in nodes) {
                tryClickToggle(node, target)
            }
            return
        }

        for (sw in switches) {
            // 检查父节点或兄弟节点是否包含"省电"文本
            var parent = sw.parent
            while (parent != null) {
                val text = parent.text?.toString() ?: ""
                val desc = parent.contentDescription?.toString() ?: ""
                if (text.contains("省电") || desc.contains("省电") ||
                    text.contains("Battery") || desc.contains("Battery")) {
                    tryClickSwitch(sw, target)
                    pendingBatteryAction = null
                    // 点完 0.5 秒后返回
                    handler.postDelayed({
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }, 500)
                    return
                }
                parent = parent.parent
            }
        }

        // 找不到精确匹配 → 尝试点击第一个 Switch
        if (switches.isNotEmpty()) {
            tryClickSwitch(switches[0], target)
            pendingBatteryAction = null
            handler.postDelayed({
                performGlobalAction(GLOBAL_ACTION_BACK)
            }, 500)
        }
    }

    private fun tryClickSwitch(sw: AccessibilityNodeInfo, target: Boolean) {
        val isChecked = sw.isChecked
        if (isChecked != target) {
            sw.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    private fun tryClickToggle(node: AccessibilityNodeInfo, target: Boolean) {
        if (node.isCheckable) {
            if (node.isChecked != target) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    fun switchToDaily() { helper.setDailyMode() }
    fun switchToGame() { helper.setGameMode() }
}
