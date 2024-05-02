package id.psw.floatstat.providers

import android.app.ActivityManager
import android.app.Service
import android.graphics.Color
import id.psw.floatstat.InternalStatProviderService
import id.psw.floatstat.plugins.PluginData

internal class RamUsageProvider(val ctx: InternalStatProviderService) : IProvider() {
    private var usedMem = -1
    private var shouldUpdate = true
    private val dat = PluginData()
    private var shouldReset = true

    override fun updateData(){
        val actMan = ctx.getSystemService(Service.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actMan.getMemoryInfo(memInfo)
        usedMem = ((1.0f - (memInfo.availMem.toFloat() / memInfo.totalMem.toFloat())) * 100).toInt()
        shouldUpdate = true
    }



    override fun init(){
        dat.textColor.update(Color.WHITE)
        dat.iconValue.update(ctx.iconUris[InternalStatProviderService.IC_RAM_USAGE]!!)
        shouldReset = false
    }

    override fun getData(): PluginData {
        if(shouldReset) dat.reset()
        shouldReset = true

        if(shouldUpdate){
            val tmpStr = if(usedMem >= 0) "${usedMem}%" else "???"
            dat.textValue.update(tmpStr)
            val bgColor = when(usedMem){
                in 90 .. 100 -> Color.RED
                in 85 .. 90 -> InternalStatProviderService.ORANGE
                in 80 .. 85 -> Color.YELLOW
                in 0 .. 80 -> Color.WHITE
                else -> Color.DKGRAY
            }
            dat.iconColor.update(bgColor)
        }
        return dat
    }

    override fun close() {
    }
}
