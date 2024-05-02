package id.psw.floatstat.providers

import android.net.TrafficStats
import id.psw.floatstat.InternalStatProviderService

internal class DataDownlinkProvider(val _ctx: InternalStatProviderService) : IDeltaNetProvider(_ctx){
    override val iconId: Int = InternalStatProviderService.IC_DOWNLINK

    override val dataGetter: Long
        get() = TrafficStats.getTotalRxBytes()
}
