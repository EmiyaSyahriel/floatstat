package id.psw.floatstat.plugin_example

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.IBinder
import android.util.Log
import id.psw.floatstat.IFloatStatDataPlugin;
import id.psw.floatstat.plugins.PluginData
import java.util.*

class SamplePluginService : Service() {

    companion object {
        const val PLUGIN_NAME_1 = "sample_plugin::rng"
        const val PLUGIN_NAME_2 = "sample_plugin::clock_time"
        val blankPluginData = PluginData()
        const val TAG = "SamplePluginService"
    }

    private var sender = ""
    open class PluginDataProvider {
        open fun getData() : PluginData = blankPluginData
    }
    private val clock = object : PluginDataProvider() {
        private var hasIconRequested = false

        override fun getData(): PluginData {
            val retval = PluginData()
            if(!hasIconRequested){
                retval.iconValue.update(IconProvider.createIconUri(R.drawable.ic_plugin_clock))
                grantUriPermission(sender, retval.iconValue.value, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                retval.iconColor.update(Color.WHITE)
                retval.textColor.update(Color.WHITE)
                hasIconRequested = true
            }

            val c = Calendar.getInstance()
            val h = c.get(Calendar.HOUR)  .toString().padStart(2, '0')
            val m = c.get(Calendar.MINUTE).toString().padStart(2, '0')
            val s = c.get(Calendar.SECOND).toString().padStart(2, '0')
            retval.textValue.update("$h:$m:$s")

            return retval
        }
    }
    private data class RNGNames (val rarity:Int, val element:Int, val name:String)
    private val rng = object : PluginDataProvider() {
        private var hasIconRequested = false
        private var lastRequestMs = 0L
        private var rngName = arrayOf(
            RNGNames(3, 0, "Milleue"),
            RNGNames(3, 1, "Selma"),
            RNGNames(4, 0, "Pirika"),
            RNGNames(4, 2, "Talia"),
            RNGNames(4, 2, "Loine"),
            RNGNames(5, 4, "Anne"),
            RNGNames(5, 1, "Nana"),
            RNGNames(6, 3, "Lilah"),
            RNGNames(6, 1, "Urth"),
            RNGNames(6, 0, "Shizuna"),
            RNGNames(6, 3, "Stella"),
            RNGNames(7, 3, "Ramel"),
        )
        private val elemColors = arrayOf(
            Color.RED,
            Color.rgb(0,128,0),
            Color.CYAN,
            Color.YELLOW,
            Color.rgb(128,0,255)
        )
        private val rarityColors = arrayOf(
            Color.rgb(0x00,0x00,0x00),
            Color.rgb(0x88, 0x88,0xD0),
            Color.rgb(0x00,0xC0, 0x2E),
            Color.rgb(0x00,0x90,0x40),
            Color.rgb(0x00,0x00,0xF0),
            Color.rgb(0x00,0x80,0xF0),
            Color.rgb(0xE0,0xD0,0x30),
            Color.rgb(0xFF,0xE0,0x00),
        )

        override fun getData(): PluginData {
            val retval = PluginData()
            if(!hasIconRequested){
                retval.iconValue.update(IconProvider.createIconUri(R.drawable.ic_plugin_rng))
                grantUriPermission(sender, retval.iconValue.value, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                hasIconRequested = true
            }

            val cTime = System.currentTimeMillis()
            val delta = cTime - lastRequestMs

            if(delta > 2000L){
                val name = rngName[(Math.random() * 10000).toInt() % rngName.size]
                retval.iconColor.update(elemColors[name.element])
                retval.textColor.update(rarityColors[name.rarity])
                retval.textValue.update(name.name)
                lastRequestMs = cTime
            }

            return retval
        }
    }
    private val binder = object : IFloatStatDataPlugin.Stub() {

        override fun getDataIds(): String {
            val sb = StringBuilder()
            arrayOf(PLUGIN_NAME_1, PLUGIN_NAME_2).apply {
                forEachIndexed { i, it ->
                    sb.append(it)
                    if(i < size-1) sb.append(',')
                }
            }
            return sb.toString()
        }
        override fun getDataName(dataId: String?): String {
            return when(dataId){
                PLUGIN_NAME_1 -> { getString(R.string.plugin_name_rng) }
                PLUGIN_NAME_2 -> { getString(R.string.plugin_name_clock) }
                else -> { getString(R.string.unknown_data_name) }
            }
        }

        override fun getData(dataId: String?): PluginData {
            return when(dataId){
                PLUGIN_NAME_1 -> rng.getData()
                PLUGIN_NAME_2 -> clock.getData()
                else -> blankPluginData
            }
        }

        override fun requestStop() {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        sender = intent?.getStringExtra(PluginData.EXTRA_DATA_SENDER) ?: ""
        Log.d(TAG, "Plugin is bound by $sender")
        return binder
    }
}