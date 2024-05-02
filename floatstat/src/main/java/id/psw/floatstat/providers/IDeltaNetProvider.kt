package id.psw.floatstat.providers

import android.graphics.Color
import id.psw.floatstat.App
import id.psw.floatstat.InternalStatProviderService
import id.psw.floatstat.plugins.PluginData

internal abstract class IDeltaNetProvider (val ctx: InternalStatProviderService): IProvider(){
    private var lastMs = 0L
    private var lastData = 0L

    abstract val dataGetter : Long
    abstract val iconId : Int

    private val data = PluginData()
    private var shouldReset = true

    override fun init() {
        data.iconColor.update(Color.WHITE)
        data.textColor.update(Color.WHITE)
        data.iconValue.update(ctx.iconUris[iconId]!!)
        shouldReset = false
    }

    override fun updateData() { }
    override fun close() { }

    override fun getData(): PluginData {
        val dat = data

        if(shouldReset) dat.reset()
        shouldReset = true

        val cMs = System.currentTimeMillis()
        val dMs = cMs - lastMs
        if(dMs > ctx.updateFrequency){
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