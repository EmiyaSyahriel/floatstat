package id.psw.floatstat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.BatteryManager
import id.psw.floatstat.providers.BatteryStateData

class BatteryStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0).toFloat()
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0).toFloat()
            BatteryStateData.power = level / scale
            BatteryStateData.isCharging = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
            BatteryStateData.temperature = intent.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE, -1)

            BatteryStateData.percIcon = when {
                BatteryStateData.power < 0.15 -> InternalStatProviderService.IC_BATTERY_LOW
                BatteryStateData.isCharging -> InternalStatProviderService.IC_BATTERY_CHARGE
                else -> InternalStatProviderService.IC_BATTERY_LEVEL
            }

            BatteryStateData.percColor = if(BatteryStateData.isCharging) Color.YELLOW else when(BatteryStateData.power){
                in 0.25f .. 0.3f  -> Color.YELLOW
                in 0.15f .. 0.25f -> Color.rgb(255,128,0)
                in 0.0f .. 0.15f  -> Color.RED
                Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY -> Color.DKGRAY
                else -> Color.WHITE
            }
            val nrmTemp = BatteryStateData.temperature / 10.0f
            if(context is InternalStatProviderService){
                BatteryStateData.tempColor = context.tempColor(nrmTemp)
            }
        }
    }
}