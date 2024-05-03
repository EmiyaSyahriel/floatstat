package id.psw.floatstat.providers

import android.content.Context
import id.psw.floatstat.IFloatStatDataPlugin
import id.psw.floatstat.InternalStatProviderService
import id.psw.floatstat.R
import id.psw.floatstat.app
import id.psw.floatstat.plugins.PluginData
import java.lang.StringBuilder
import java.util.Timer
import java.util.prefs.Preferences
import kotlin.concurrent.timerTask

class InternalStatProvider(val ctx: InternalStatProviderService) : IFloatStatDataPlugin {

    companion object {
        const val PK_UPDATE_RATE = "UpdateRate"
        const val UPDATE_RATE_LOWEST = 10L
    }

    private val batteryPercData = BatteryPercProvider(ctx)
    private val batteryTempData = BatteryTempProvider(ctx)
    private val cpuTempData = CpuTempProvider(ctx)
    private val ramUsageData = RamUsageProvider(ctx)
    private val dnLinkData = DataDownlinkProvider(ctx)
    private val upLinkData = DataUplinkProvider(ctx)

    private val providers : Array<IProvider> by lazy {
        arrayOf(batteryTempData, batteryPercData, cpuTempData, ramUsageData, dnLinkData, upLinkData)
    }

    private val mainTimer = Timer("Internal Stat Updater")
    private var keepRunning = true

    private fun doUpdate()
    {
        providers.forEach { it.updateData() }

        if(!keepRunning) return

        val update = ctx.app.pref.getLong(PK_UPDATE_RATE, 100L).coerceAtLeast(UPDATE_RATE_LOWEST)
        mainTimer.schedule( timerTask { doUpdate() }, update)
    }

    internal fun initialize(){
        providers.forEach { it.init() }
        doUpdate()
    }

    override val dataIds : String get(){
        val sb = StringBuilder()
        arrayOf(
            InternalStatProviderService.DAT_TEMP_CPU,
            InternalStatProviderService.DAT_TEMP_BATTERY,
            InternalStatProviderService.DAT_RAM_USAGE,
            InternalStatProviderService.DAT_BATTERY_INFO,
            InternalStatProviderService.DAT_NET_UPLOAD,
            InternalStatProviderService.DAT_NET_DNLOAD,
        ).apply {
            forEachIndexed { i, s ->
                sb.append(s)
                if(i < size-1) sb.append(',')
            }
        }
        return sb.toString()
    }

    override fun getDataName(dataId: String): String {
        return when(dataId) {
            InternalStatProviderService.DAT_BATTERY_INFO -> ctx.getString(R.string.internal_stat_battery_info)
            InternalStatProviderService.DAT_NET_DNLOAD -> ctx.getString(R.string.internal_stat_downlink)
            InternalStatProviderService.DAT_NET_UPLOAD -> ctx.getString(R.string.internal_stat_uplink)
            InternalStatProviderService.DAT_RAM_USAGE -> ctx.getString(R.string.internal_stat_ram_usage)
            InternalStatProviderService.DAT_TEMP_BATTERY -> ctx.getString(R.string.internal_stat_temp_bat)
            InternalStatProviderService.DAT_TEMP_CPU -> ctx.getString(R.string.internal_stat_temp_cpu)
            else -> ctx.getString(R.string.unknown)
        }
    }

    override fun getData(dataId: String): PluginData {
        return when(dataId){
            InternalStatProviderService.DAT_BATTERY_INFO -> batteryPercData.getData()
            InternalStatProviderService.DAT_TEMP_BATTERY -> batteryTempData.getData()
            InternalStatProviderService.DAT_TEMP_CPU ->     cpuTempData.getData()
            InternalStatProviderService.DAT_RAM_USAGE ->    ramUsageData.getData()
            InternalStatProviderService.DAT_NET_DNLOAD ->   dnLinkData.getData()
            InternalStatProviderService.DAT_NET_UPLOAD ->   upLinkData.getData()
            else -> InternalStatProviderService.blankPluginData
        }
    }

    override fun requestStop() {
        keepRunning = false
        providers.forEach { it.close() }
        mainTimer.cancel()
        mainTimer.purge()
        ctx.stopSelf()
    }
}