package id.psw.floatstat.plugin_example.providers

import android.content.Intent
import android.graphics.Color
import id.psw.floatstat.plugin_example.IconProvider
import id.psw.floatstat.plugin_example.R
import id.psw.floatstat.plugin_example.SamplePluginService
import id.psw.floatstat.plugins.PluginData
import java.util.Calendar

class ClockProvider(ctx: SamplePluginService) : PluginDataProvider(ctx) {
    private var hasIconRequested = false

    override fun getData(): PluginData {
        val retval = PluginData()
        if(!hasIconRequested){
            retval.iconValue.update(IconProvider.createIconUri(R.drawable.ic_plugin_clock))
            ctx.grantUriPermission(ctx.sender, retval.iconValue.value, Intent.FLAG_GRANT_READ_URI_PERMISSION)
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