package com.mitool

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * 无障碍服务 — 备选激活方案。
 * 开启后，可通过外部广播或通知触发模式切换。
 * 用户需在「设置 → 无障碍 → 已安装的应用」中手动开启。
 */
class MiAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MiAccessibilityService? = null
            private set

        const val ACTION_DAILY = "com.mitool.ACTION_DAILY"
        const val ACTION_GAME = "com.mitool.ACTION_GAME"
    }

    private val helper by lazy { SettingsHelper(this) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 可用于监听特定应用启动等场景
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * 通过无障碍服务执行操作（拥有更高权限）
     */
    fun switchToDaily() {
        helper.setDailyMode()
    }

    fun switchToGame() {
        helper.setGameMode()
    }
}
