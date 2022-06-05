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
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.lang.StringBuilder

class InternalStatProviderService : Service() {
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

    private val iconUris = mutableMapOf<Int, Uri>()
    private var updateFrequency = 1000L

    @SuppressLint("UseCompatLoadingForDrawables") // We need to support KitKat!
    private fun loadIcons() {
        val density = resources.displayMetrics.density
        val iconSize = (64 * density).toInt()

        val atlas =
            resources.getDrawable(R.drawable.sheet_icons).toBitmap(iconSize * 4, iconSize * 2, Bitmap.Config.ALPHA_8)
        val uvRect = Rect()
        val vpRect = Rect()
        arrayOf(
            arrayOf(IC_BATTERY_LEVEL, IC_BATTERY_CHARGE, IC_BATTERY_LOW, IC_RAM_USAGE),
            arrayOf(IC_DOWNLINK, IC_UPLINK, IC_CPU_TEMP, IC_BAT_TEMP)
        ).forEachIndexed { yIdx, yArr ->
            yArr.forEachIndexed { xIdx, id ->
                val x = xIdx * iconSize
                val y = yIdx * iconSize
                val ctBmp = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ALPHA_8)
                val drawCnv = Canvas(ctBmp)
                uvRect.set(x, y, x + iconSize, y + iconSize)
                vpRect.set(0,0,iconSize,iconSize)
                drawCnv.drawBitmap(atlas, uvRect, vpRect, null)
                iconUris[id] = app().iconProvider.registerIcon(id, ctBmp)
            }
        }
    }

    override fun onCreate() {
        loadIcons()
        super.onCreate()
    }

    private open class IPlugin {
        private var _isInit = true
        protected val isInit : Boolean get() = _isInit
        private var isFirstCall = true

        open fun getData() : PluginData {
            if(!isFirstCall && _isInit) {
                _isInit = false
            }
            isFirstCall = false
            return PluginData()
        }
        open fun close() {}
    }

    object BatteryStateData {
        var power = 0.0f
        var temperature = 0
        var isCharging = false
        var percIcon = 0
        var percColor = Color.RED
        var tempColor = Color.WHITE
    }

    private val batteryPercData = object : IPlugin() {
        private var lastIcon = 0
        private var lastColor = 0xFFFF

        override fun getData(): PluginData {
            val dat = super.getData()
            if(isInit){
                dat.textColor.update(Color.WHITE)
            }
            dat.textValue.update("${(BatteryStateData.power * 100).toInt()}%")

            if(lastIcon != BatteryStateData.percIcon){
                dat.iconValue.update(iconUris[BatteryStateData.percIcon])
                lastIcon = BatteryStateData.percIcon
            }
            if(lastColor != BatteryStateData.percColor){
                dat.iconColor.update(BatteryStateData.percColor)
                lastColor = BatteryStateData.percColor
            }

            return dat
        }
    }

    private fun tempColor(temp:Float) : Int{
        return when(temp){
            in 45.0f .. Float.MAX_VALUE -> Color.RED
            in 41.0f .. 45.0f -> ORANGE
            in 38.0f .. 41.0f -> Color.YELLOW
            in 35.0f .. 38.0f -> Color.GREEN
            Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY -> Color.DKGRAY
            else -> Color.WHITE
        }
    }

    private val batteryTempData = object : IPlugin(){
        private var lastColor = 0x09F

        override fun getData(): PluginData {
            val dat = super.getData()
            if(isInit){
                dat.iconValue.update(iconUris[IC_BAT_TEMP])
            }

            if(BatteryStateData.tempColor != lastColor){
                dat.iconColor.update(BatteryStateData.tempColor)
                lastColor = BatteryStateData.tempColor
            }
            dat.textValue.update("${BatteryStateData.temperature / 10.0f}$degree")
            return dat
        }
    }

    private val batteryStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null){
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0).toFloat()
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0).toFloat()
                BatteryStateData.power = level / scale
                BatteryStateData.isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
                BatteryStateData.temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)

                BatteryStateData.percIcon = when {
                    BatteryStateData.power < 0.15 -> IC_BATTERY_LOW
                    BatteryStateData.isCharging -> IC_BATTERY_CHARGE
                    else -> IC_BATTERY_LEVEL
                }

                BatteryStateData.percColor = if(BatteryStateData.isCharging) Color.YELLOW else when(BatteryStateData.power){
                    in 0.3f .. 0.25f  -> Color.YELLOW
                    in 0.25f .. 0.15f -> Color.rgb(255,128,0)
                    in 0.15f .. 0.0f  -> Color.RED
                    Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY -> Color.DKGRAY
                    else -> Color.WHITE
                }
                val nrmTemp = BatteryStateData.temperature / 10.0f
                BatteryStateData.tempColor = tempColor(nrmTemp)
            }
        }
    }

    private open inner class IDeltaNetPlugin : IPlugin(){
        private var lastMs = 0L
        private var lastData = 0L

        open val dataGetter = 0L
        open val iconId = 0

        override fun getData(): PluginData {
            val dat = super.getData()

            if(isInit){
                dat.iconColor.update(Color.WHITE)
                dat.textColor.update(Color.WHITE)
                dat.iconValue.update(iconUris[iconId]!!)
            }

            val cMs = System.currentTimeMillis()
            val dMs = cMs - lastMs
            if(dMs > updateFrequency){
                lastMs = cMs
                val cDat = dataGetter
                val dDat = cDat - lastData
                val fSec = dMs / 1000.0f
                lastData = cDat
                dat.textValue.update(App.speedFormatter((dDat / fSec).toLong(), false))
            }
            return dat
        }
    }

    private val upLinkData = object : IDeltaNetPlugin(){
        override val iconId: Int = IC_UPLINK

        override val dataGetter: Long
            get() = TrafficStats.getMobileTxBytes()
    }

    private val dnLinkData = object : IDeltaNetPlugin(){
        override val iconId: Int = IC_DOWNLINK

        override val dataGetter: Long
            get() = TrafficStats.getMobileRxBytes()
    }

    private val cpuTempData = object : IPlugin(){
        @Volatile private var keepRunning = true
        @Volatile private var shouldUpdate = true
        @Volatile private var tSenseSensorTemp = Float.NaN

        private fun threadFunc(){val thermalDir = File("/sys/class/thermal")
            val tempData = mutableMapOf<String, Float>()
            val cpuTempData = arrayListOf<Float>()
            val cth = Thread.currentThread()
            while(keepRunning && !cth.isInterrupted){
                thermalDir.list { dir, _ -> dir.isDirectory }?.forEach {
                    try{
                        val ls = File(thermalDir, it)
                        val typeFile = File(ls, "type")
                        val tempFile = File(ls, "temp")
                        if(typeFile.exists() && tempFile.exists()){
                            val typeRac = RandomAccessFile(typeFile, "r")
                            val tempRac = RandomAccessFile(tempFile, "r")
                            val type = typeRac.readLine().trim()
                            val temp = tempRac.readLine().trim()
                            typeRac.close()
                            tempRac.close()
                            val scale = when(temp.length){
                                1, 2 -> 1.0f
                                3 -> 10.0f
                                4 -> 100.0f
                                5 -> 1000.0f
                                else -> 1.0f
                            }
                            tempData[type.trim()] = temp.toFloat() / scale
                        }

                    }catch(e: Exception){}
                }
                cpuTempData.clear()
                tempData.forEach {
                    if(it.key.contains("tz")){
                        cpuTempData.add(it.value)
                    }
                }
                tSenseSensorTemp = if(cpuTempData.size > 0){
                    cpuTempData.average().toFloat()
                }else{
                    Float.NaN
                }
                shouldUpdate= true
                Thread.sleep(updateFrequency)
            }
        }
        private val thread = Thread { threadFunc() }.apply { name = "tCpuTempReader" }

        private fun init(){
            thread.start()
        }

        override fun getData(): PluginData {
            val dat = super.getData()
            if(isInit){
                init()
                dat.iconColor.update(Color.WHITE)
                dat.iconValue.update(iconUris[IC_CPU_TEMP]!!)
            }
            if(shouldUpdate){
                val temp = tSenseSensorTemp
                var tempStr = "???"
                var color = Color.GRAY
                if(tSenseSensorTemp.isFinite()){
                    tempStr = "${"%.1f".format(temp)}$degree"
                    color = tempColor(tSenseSensorTemp - 10.0f)
                }
                dat.textValue.update(tempStr)
                dat.textColor.update(color)
                shouldUpdate = false
            }

            return dat
        }

        override fun close() {
            keepRunning = false
            thread.interrupt()
            super.close()
        }
    }

    private val ramStageData = object :IPlugin(){
        private var keepRunning = true
        private var usedMem = -1
        private var shouldUpdate = true
        private fun threadFunc(){
            val actMan = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            while(keepRunning){
                actMan.getMemoryInfo(memInfo)
                usedMem = ((1.0f - (memInfo.availMem.toFloat() / memInfo.totalMem.toFloat())) * 100).toInt()
                shouldUpdate = true
                Thread.sleep(updateFrequency)
            }
        }
        private val thread = Thread { threadFunc() }.apply { name = "tRamStage" }

        private fun init(){
            thread.start()
        }

        override fun getData(): PluginData {
            val dat = super.getData()
            if(isInit){
                init()
                dat.textColor.update(Color.WHITE)
                dat.iconValue.update(iconUris[IC_RAM_USAGE]!!)
            }
            if(shouldUpdate){
                val tmpStr = if(usedMem >= 0) "${usedMem}%" else "???"
                dat.textValue.update(tmpStr)
                val bgColor = when(usedMem){
                    in 90 .. 100 -> Color.RED
                    in 85 .. 90 -> ORANGE
                    in 80 .. 85 -> Color.YELLOW
                    in 0 .. 80 -> Color.WHITE
                    else -> Color.DKGRAY
                }
                dat.iconColor.update(bgColor)
            }
            return dat
        }

        override fun close() {
            keepRunning = false
            thread.interrupt()
            super.close()
        }
    }

    private val binder = object : IFloatStatDataPlugin.Stub() {
        override fun getDataIds(): String {
            val sb = StringBuilder()
            arrayOf(
                DAT_TEMP_CPU,
                DAT_TEMP_BATTERY,
                DAT_RAM_USAGE,
                DAT_BATTERY_INFO,
                DAT_NET_UPLOAD,
                DAT_NET_DNLOAD,
            ).apply {
                forEachIndexed { i, s ->
                    sb.append(s)
                    if(i < size-1) sb.append(',')
                }
            }
            return sb.toString()
        }

        override fun getPluginName(): String {
            return getString(R.string.internal_plugin_name)
        }

        override fun getDataName(dataId: String?): String {
            return when(dataId) {
                DAT_BATTERY_INFO -> getString(R.string.internal_stat_battery_info)
                DAT_NET_DNLOAD -> getString(R.string.internal_stat_downlink)
                DAT_NET_UPLOAD -> getString(R.string.internal_stat_uplink)
                DAT_RAM_USAGE -> getString(R.string.internal_stat_ram_usage)
                DAT_TEMP_BATTERY -> getString(R.string.internal_stat_temp_bat)
                DAT_TEMP_CPU -> getString(R.string.internal_stat_temp_cpu)
                else -> getString(R.string.unknown)
            }
        }

        override fun getData(dataId: String?): PluginData {
            return when(dataId){
                DAT_BATTERY_INFO -> batteryPercData.getData()
                DAT_TEMP_BATTERY -> batteryTempData.getData()
                DAT_TEMP_CPU -> cpuTempData.getData()
                DAT_RAM_USAGE -> ramStageData.getData()
                DAT_NET_DNLOAD -> dnLinkData.getData()
                DAT_NET_UPLOAD -> upLinkData.getData()
                else -> blankPluginData
            }
        }

        override fun requestStop() {
            batteryPercData.close()
            stopSelf()
        }
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