package id.psw.floatstat.plugin_example

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import id.psw.floatstat.IFloatStatDataPlugin_Stub
import id.psw.floatstat.plugin_example.impls.SamplePluginImpl
import id.psw.floatstat.plugin_example.providers.*
import id.psw.floatstat.plugins.PluginData

class SamplePluginService : Service() {

    companion object {
        const val PLUGIN_NAME_1 = "sample_plugin::rng"
        const val PLUGIN_NAME_2 = "sample_plugin::clock_time"
        val blankPluginData = PluginData()
        const val TAG = "SamplePluginService"
    }

    internal var sender = ""
    internal val rng = RngProvider(this)
    internal val clock = ClockProvider(this)

    private val binder : IBinder by lazy {
        IFloatStatDataPlugin_Stub(SamplePluginImpl(this))
    }

    override fun onBind(intent: Intent?): IBinder {
        sender = intent?.getStringExtra(PluginData.EXTRA_DATA_SENDER) ?: ""
        Log.d(TAG, "Plugin is bound by $sender")
        return binder
    }
}