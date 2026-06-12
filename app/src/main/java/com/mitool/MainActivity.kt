package com.mitool

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var helper: SettingsHelper
    private lateinit var tvRootStatus: TextView
    private lateinit var tvAccessStatus: TextView
    private lateinit var tvTileHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helper = SettingsHelper(this)

        tvRootStatus = findViewById(R.id.tv_root_status)
        tvAccessStatus = findViewById(R.id.tv_access_status)
        tvTileHint = findViewById(R.id.tv_tile_hint)

        // 请求 WRITE_SETTINGS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            })
        }

        refreshStatus()

        // 启动通知栏后台服务
        try { startService(Intent(this, SwitchService::class.java)) } catch (e: Exception) { android.util.Log.e("MITool", "startService failed", e) }

        // ===== 按钮 =====
        findViewById<Button>(R.id.btn_daily).setOnClickListener {
            helper.setDailyMode()
            Toast.makeText(this, "已切换：☀️ 日常模式", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_game).setOnClickListener {
            helper.setGameMode()
            Toast.makeText(this, "已切换：🎮 游戏模式", Toast.LENGTH_SHORT).show()
        }

        // 打开无障碍设置
        findViewById<Button>(R.id.btn_open_access).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "找到「小米快捷」并开启", Toast.LENGTH_LONG).show()
        }

        // 打开快捷开关编辑
        findViewById<Button>(R.id.btn_edit_tiles).setOnClickListener {
            // 下拉通知栏编辑页 — 部分 MIUI 支持
            try {
                startActivity(Intent("android.settings.QS_EDIT_SETTINGS"))
            } catch (e: Exception) {
                Toast.makeText(this, "请下拉通知栏 → 点编辑按钮 → 添加「小米快捷」磁贴", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        // Root 检测
        val hasRoot = RootHelper.isRootAvailable()
        tvRootStatus.text = if (hasRoot) "✅ Root 已授权（高权限模式）" else "❌ 未检测到 Root"
        tvRootStatus.setTextColor(if (hasRoot) 0xFF4CAF50.toInt() else 0xFFFF5722.toInt())

        // 无障碍检测
        val accessOn = isAccessibilityEnabled()
        tvAccessStatus.text = if (accessOn) "✅ 无障碍已开启" else "❌ 无障碍未开启"
        tvAccessStatus.setTextColor(if (accessOn) 0xFF4CAF50.toInt() else 0xFFFF5722.toInt())

        // 激活方案提示
        tvTileHint.text = if (hasRoot)
            "当前激活方案：Root（完全自动化，无需额外授权）"
        else if (accessOn)
            "当前激活方案：无障碍（中等权限，部分设置需手动确认）"
        else
            "⚠️ 未检测到激活方案，按钮可能无法生效。请开启 Root 或无障碍服务。"
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "$packageName/.MiAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(service) || enabledServices.contains("com.mitool")
    }
}
