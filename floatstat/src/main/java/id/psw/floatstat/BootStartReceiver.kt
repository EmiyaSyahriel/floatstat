package id.psw.floatstat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null){
            if(context.app().startOnBoot){
                if(intent?.action == Intent.ACTION_BOOT_COMPLETED){
                    FloatWindowService.startServiceS(context)
                }
            }
        }
    }
}