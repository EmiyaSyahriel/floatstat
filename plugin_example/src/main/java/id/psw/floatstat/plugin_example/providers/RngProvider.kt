package id.psw.floatstat.plugin_example.providers

import android.content.Intent
import android.graphics.Color
import id.psw.floatstat.plugin_example.IconProvider
import id.psw.floatstat.plugin_example.R
import id.psw.floatstat.plugin_example.SamplePluginService
import id.psw.floatstat.plugins.PluginData

class RngProvider(ctx: SamplePluginService) : PluginDataProvider(ctx) {

    private data class RNGNames (val rarity:Int, val element:Int, val name:String)

    private var hasIconRequested = false
    private var lastRequestMs = 0L

    // This is character names, spellchecker have no authorization to
    // correct each of these beautifully-crafted character names!
    @Suppress("SpellCheckingInspection")
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
            ctx.grantUriPermission(ctx.sender, retval.iconValue.value, Intent.FLAG_GRANT_READ_URI_PERMISSION)
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