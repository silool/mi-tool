package com.mitool

import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ModeTileService : TileService() {

    companion object {
        const val PREFS = "mode_prefs"
        const val KEY_MODE = "current_mode"
        const val MODE_DAILY = "daily"
        const val MODE_GAME = "game"
    }

    private val prefs: SharedPreferences
        get() = getSharedPreferences(PREFS, MODE_PRIVATE)

    private val helper by lazy { SettingsHelper(this) }

    override fun onTileAdded() {
        setToDefault()
    }

    override fun onStartListening() {
        updateTileUI()
    }

    override fun onClick() {
        val current = prefs.getString(KEY_MODE, MODE_DAILY) ?: MODE_DAILY
        if (current == MODE_DAILY) {
            helper.setGameMode()
            prefs.edit().putString(KEY_MODE, MODE_GAME).apply()
        } else {
            helper.setDailyMode()
            prefs.edit().putString(KEY_MODE, MODE_DAILY).apply()
        }
        updateTileUI()
    }

    private fun updateTileUI() {
        val mode = prefs.getString(KEY_MODE, MODE_DAILY) ?: MODE_DAILY
        qsTile?.apply {
            if (mode == MODE_DAILY) {
                label = "日常模式"
                state = Tile.STATE_ACTIVE
            } else {
                label = "游戏模式"
                state = Tile.STATE_INACTIVE
            }
            updateTile()
        }
    }

    private fun setToDefault() {
        prefs.edit().putString(KEY_MODE, MODE_DAILY).apply()
        updateTileUI()
    }
}
