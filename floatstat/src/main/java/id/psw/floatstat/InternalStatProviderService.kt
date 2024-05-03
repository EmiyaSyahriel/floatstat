package id.psw.floatstat

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.net.TrafficStats
import android.net.Uri
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.graphics.drawable.toBitmap
import id.psw.floatstat.plugins.PluginData
import id.psw.floatstat.providers.InternalStatProvider
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception

class InternalStatProviderService : Service() {
    private val batteryStateReceiver = BatteryStateReceiver()

    companion object {
        const val DAT_BATTERY_INFO    = "internal::battery"
        const val DAT_TEMP_CPU        = "internal::cputemp"
        const val DAT_TEMP_BATTERY    = "internal::battemp"
        const val DAT_RAM_USAGE       = "internal::ram_usage"
        const val DAT_NET_DNLOAD      = "internal::download"
        const val DAT_NET_UPLOAD      = "internal::upload"

        const val IC_BATTERY_LEVEL    = 0xBA77371
        const val IC_BATTERY_LOW      = 0xBA77703
        const val IC_BATTERY_CHARGE   = 0xBA7C463
        const val IC_RAM_USAGE        = 0x333071F
        const val IC_DOWNLINK         = 0xFFD7126
        const val IC_UPLINK           = 0x0077126
        const val IC_CPU_TEMP         = 0xC700733
        const val IC_BAT_TEMP         = 0xBA77337

        const val degree = '°'
        val ORANGE = Color.rgb(255, 128,0)

        val blankPluginData = PluginData()
    }

    internal val iconUris = mutableMapOf<Int, Uri>()
    internal var updateFrequency = 1000L

    @SuppressLint("UseCompatLoadingForDrawables") // We need to support KitKat!
    private fun loadIcons() {
        val density = resources.displayMetrics.density
        val iconSize = (64 * density).toInt()

        @Suppress("DEPRECATION") val atlas =
            resources.getDrawable(R.drawable.sheet_icons)
                .toBitmap(iconSize * 4, iconSize * 2, Bitmap.Config.ALPHA_8)
        val uvRect = Rect()
        val vpRect = Rect()
        arrayOf(
            arrayOf(IC_BATTERY_LEVEL, IC_BATTERY_CHARGE, IC_BATTERY_LOW, IC_RAM_USAGE),
            arrayOf(IC_DOWNLINK, IC_UPLINK, IC_CPU_TEMP, IC_BAT_TEMP)
        ).forEachIndexed { yIdx, yArr ->
            yArr.forEachIndexed { xIdx, id ->
                val x = xIdx * iconSize
                val y = yIdx * iconSize
                val ctBmp = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
                val drawCnv = Canvas(ctBmp)
                uvRect.set(x, y, x + iconSize, y + iconSize)
                vpRect.set(0,0,iconSize,iconSize)
                drawCnv.drawBitmap(atlas, uvRect, vpRect, null)
                iconUris[id] = app.iconProvider.registerIcon(id, ctBmp)
            }
        }
    }

    override fun onCreate() {
        loadIcons()
        super.onCreate()
    }


    internal fun tempColor(temp:Float) : Int{
        return when(temp){
            in 45.0f .. Float.MAX_VALUE -> Color.RED
            in 41.0f .. 45.0f -> ORANGE
            in 38.0f .. 41.0f -> Color.YELLOW
            in 30.0f .. 38.0f -> Color.GREEN
            in 20.0f .. 30.0f -> Color.CYAN
            Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY -> Color.DKGRAY
            else -> Color.WHITE
        }
    }

    private val binder : IBinder by lazy {
        IFloatStatDataPlugin_Stub(InternalStatProvider(this).apply {
            initialize()
        })
    }

    override fun onBind(intent: Intent?): IBinder {
        registerReceiver(batteryStateReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return binder
    }

    override fun onDestroy() {
        unregisterReceiver(batteryStateReceiver)
        super.onDestroy()
    }
}