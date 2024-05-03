package id.psw.floatstat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.concurrent.timer

@RequiresApi(Build.VERSION_CODES.N)
class SettingTileService : TileService() {

    companion object{
        fun update(ctx:Context){
            TileService.requestListeningState(ctx, ComponentName(ctx, SettingTileService::class.java))
        }
    }

    private val t get()= qsTile
    private lateinit var _cachedIcon : Icon
    private var canUpdate = false
    private lateinit var updateTimer : Timer
    private var isChangePending = false

    override fun onCreate() {
        super.onCreate()
        _cachedIcon =Icon.createWithResource(this, R.drawable.ic_main_notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        requestListeningState(this, ComponentName(this, SettingTileService::class.java))
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStartListening() {
        canUpdate = true
        isChangePending = false
        internalUpdateTile()
    }

    override fun onStopListening() {
        canUpdate = false
    }

    override fun onTileRemoved() {
        canUpdate = false
    }

    private fun internalUpdateTile(){
        if(!canUpdate) return
        val pending = isChangePending
        t.icon = _cachedIcon

        if(app.isFloatServiceRunning){
            val isVisible =app.isFloatWindowVisible
            t.state = isVisible.select(Tile.STATE_ACTIVE, Tile.STATE_INACTIVE)
            setDescription(
                pending.select(getString(R.string.tile_visibility_pending), isVisible.select(getString(
                                    R.string.tile_visibility_visible), getString(R.string.tile_visibility_hidden))))
        }else{
            t.state = Tile.STATE_UNAVAILABLE
            setDescription(pending.select(getString(R.string.tile_started_pending), getString(R.string.tile_started_no)))
        }
        t.updateTile()
        isChangePending = false
    }

    private fun setDescription(str:String){
        if(sdkAtLeast(Build.VERSION_CODES.Q)) t.subtitle = str
    }

    override fun onClick() {
        unlockAndRun {
            if(app.isFloatServiceRunning){
                startService(Intent(FloatWindowService.ACTION_VISIBILITY).setClass(app, FloatWindowService::class.java))
            }else{
                FloatWindowService.startServiceS(app)
            }
            isChangePending = true
            internalUpdateTile()
        }
    }
}