package id.psw.floatstat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootStartReceiver : BroadcastReceiver() {
    companion object {
        private val bootActions : Array<String> = arrayOf(
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_REBOOT, // MIUI
            "com.htc.action.QUICKBOOT_POWERON", // HTC
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null && intent != null){
            val a = intent.action
            if(bootActions.indexOf(a) >= 0){
                FloatWindowService.startServiceS(context)
            }
        }
    }
}