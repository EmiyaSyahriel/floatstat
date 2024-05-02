package id.psw.floatstat.providers

import android.graphics.Color
import id.psw.floatstat.InternalStatProviderService
import id.psw.floatstat.plugins.PluginData

internal class BatteryPercProvider(val ctx:InternalStatProviderService) : IProvider() {
    private var lastIcon = 0
    private var lastColor = 0xFFFF

    private val dat = PluginData()
    private var shouldReset = true

    override fun init() {
        dat.textColor.update(Color.WHITE)
        shouldReset = false
    }

    override fun getData(): PluginData {
        if(shouldReset) dat.reset()
        shouldReset = true

        dat.textValue.update("${(BatteryStateData.power * 100).toInt()}%")

        if(lastIcon != BatteryStateData.percIcon){
            dat.iconValue.update(ctx.iconUris[BatteryStateData.percIcon])
            lastIcon = BatteryStateData.percIcon
        }
        if(lastColor != BatteryStateData.percColor){
            dat.iconColor.update(BatteryStateData.percColor)
            lastColor = BatteryStateData.percColor
        }

        return dat
    }

    override fun updateData() {
    }

    // Nothing to cleanup
    override fun close() {}
}