package id.psw.floatstat.providers

import android.net.TrafficStats
import id.psw.floatstat.InternalStatProviderService

internal class DataUplinkProvider(val _ctx: InternalStatProviderService) : IDeltaNetProvider(_ctx) {
    override val iconId: Int = InternalStatProviderService.IC_UPLINK

    override val dataGetter: Long
        get() = TrafficStats.getTotalTxBytes()
}