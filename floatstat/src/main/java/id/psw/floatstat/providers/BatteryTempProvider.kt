package id.psw.floatstat.providers

import id.psw.floatstat.InternalStatProviderService
import id.psw.floatstat.plugins.PluginData

internal class BatteryTempProvider(val ctx:InternalStatProviderService) : IProvider(){
    private var lastColor = 0x09F

    private val dat = PluginData()
    private var shouldReset = true

    override fun init() {
        dat.iconValue.update(ctx.iconUris[InternalStatProviderService.IC_BAT_TEMP])
        shouldReset = false
    }

    override fun getData(): PluginData {
        if(shouldReset) dat.reset()
        shouldReset = true

        if(BatteryStateData.tempColor != lastColor){
            val col = BatteryStateData.tempColor
            dat.iconColor.update(col)
            dat.textColor.update(col)
            lastColor = col
        }
        dat.textValue.update("${BatteryStateData.temperature / 10.0f}${InternalStatProviderService.degree}")
        return dat
    }

    override fun close() {
    }

    override fun updateData() {
    }
}