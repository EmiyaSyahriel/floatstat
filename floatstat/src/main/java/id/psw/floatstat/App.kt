package id.psw.floatstat

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import id.psw.floatstat.plugins.PluginData
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.text.StringBuilder

class App : Application() {

    private var keepRunning = true
    lateinit var iconProvider : InternalIconProvider
    var isFloatServiceRunning = false
    var isFloatWindowVisible = true

    @Suppress("SpellCheckingInspection") // They are intentionally mispelled so that each
                                                 // are uniquely identifiable.
    companion object {
        private val speedClassByte = arrayOf("B","k","MB","GB","TB","PB","EB","ZB","YB")
        private val speedClassBit = arrayOf("b","Kb","Mb","Gb","Tb","Pb","Eb","Zb","Yb")
        fun speedFormatter(size:Long, bit:Boolean) : String {
            var sz = size * bit.select(8,1)
            var idx = 0
            while(sz > 1000 || idx == 0){
                sz /= 1000
                idx++
            }
            return StringBuilder().append(sz.toInt()).append(bit.select(speedClassBit, speedClassByte)[idx]).toString()
        }
        const val ACTION_START_PLUGIN = "id.psw.floatstat.action.START_PLUGIN"
        const val CATEGORY_PLUGIN = "id.psw.floatstat.category.DATA_PLUGIN"
        const val PREF_NAME = "_"
        const val PK_THREADED_UPDATE_FREQ = "KeseringanPembaruan"
        const val PK_ENABLED_PLUGIN = "TetambahanAktif"
        const val PK_DEFAULT_PLUGIN_DISPLAY = "TetambahanTampil"
        const val PK_RUN_ON_STARTUP = "MulaiPasHidup"
        const val PK_HIDE_ON_TAP = "HideTap"
        const val PK_TILE_ADDED = "BeliPorselenGanWkwkwkwk"
    }


    // String Format : {pkg}/{id};
    class PluginId {
        private var _pkg: String = ""
        private var _id : String = ""

        val pkg get() = _pkg
        val id get() = _id

        constructor(fmt:String) {
            val srcFmt = fmt.split("/")
            if(srcFmt.size < 2) throw StringIndexOutOfBoundsException("Corrupted format")
            _pkg = srcFmt[0]
            _id = srcFmt[1]
        }

        constructor(pkg:String, id:String){
            _pkg = pkg
            _id = id
        }

        override fun toString(): String = "$_pkg/$_id"
        fun equals(pkg:String, id:String): Boolean = _pkg == pkg && id == _id
    }


    private var updateFrequency = 1000L
    private val ctx = this

    data class PluginDataInfo(
        val id : String,
        var displayName : String,
        var iconUri : Uri?,
        var icon : Bitmap?,
        var iconTint : Int,
        var textTint : Int,
        var value : String
    )

    data class PluginInfo (
        val binder: IFloatStatDataPlugin,
        val name : ComponentName,
        val displayName : String = binder.javaClass.name,
        val dataList : ArrayList<PluginDataInfo> = arrayListOf()){

        fun updateData(ctx:App){
            dataList.forEach {
                val eligibleToUpdate = ctx.defaultPlugin.equals(this.name.className, it.id)
                        || ctx.activePlugins.indexOfFirst { pId -> pId.equals(this.name.className, it.id) } >= 0
                if(eligibleToUpdate) {
                    val dData = binder.getData(it.id)
                    if (dData.iconValue.updated) {
                        try {
                            val ofd = ctx.contentResolver.openFileDescriptor(
                                dData.iconValue.value,
                                "r"
                            )
                            if (ofd != null) {
                                it.icon?.recycle()
                                it.icon = BitmapFactory.decodeFileDescriptor(ofd.fileDescriptor)
                            }
                            ofd?.close()
                        } catch (rte: RuntimeException) {
                            rte.printStackTrace()
                        }
                        it.iconUri = dData.iconValue.value
                        ctx.app().shouldUpdate = true
                    }
                    if (dData.iconColor.updated) {
                        it.iconTint = dData.iconColor.value
                        ctx.app().shouldUpdate = true
                    }
                    if (dData.textColor.updated) {
                        it.textTint = dData.textColor.value
                        ctx.app().shouldUpdate = true
                    }
                    if (dData.textValue.updated) {
                        it.value = dData.textValue.value
                        ctx.app().shouldUpdate = true
                    }
                }
            }
        }
    }

    val pluginList = arrayListOf<PluginInfo>()
    private val displayNames = mutableMapOf<ComponentName, String>()
    private val pluginConnector = object : ServiceConnection {
        var hasBound = false
        private val TAG = "PluginQuery"
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            hasBound = true
            if(service != null && name != null){
                val asPlugin = IFloatStatDataPlugin_Proxy(service)
                if(pluginList.indexOfFirst { it.name == name } < 0){
                    val pluginDspName = displayNames[name] ?: name.toString()
                    Log.d(TAG, "$name -> $pluginDspName")
                    val vInfo = PluginInfo(asPlugin, name, pluginDspName)
                    asPlugin.dataIds.split(',').forEach {
                        val dName = asPlugin.getDataName(it)
                        val dInfo = PluginDataInfo(
                            it, dName,
                            null, null, Color.WHITE,
                            Color.WHITE, "???")
                        vInfo.dataList.add(dInfo)
                    }
                    vInfo.updateData(ctx)
                    pluginList.add(vInfo)
                }else{
                    Log.d(TAG, "Plugin with component name $name already added!")
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            pluginList.removeAll { it.name == name }
        }
    }

    private fun listPlugins(){
        val i = Intent(ACTION_START_PLUGIN).addCategory(CATEGORY_PLUGIN)
        val dPkg = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentServices(i, PackageManager.ResolveInfoFlags.of(0L))
        } else @Suppress("DEPRECATION") { // Shut up! this is for older device the API did not even care anymore!
            packageManager.queryIntentServices(i, 0)
        }
        if(pluginConnector.hasBound){
            unbindService(pluginConnector)
            pluginConnector.hasBound = false
        }
        pluginList.clear()

        dPkg.forEach {
            try{
                val cName = ComponentName(it.serviceInfo.packageName, it.serviceInfo.name)
                val sbi = Intent(ACTION_START_PLUGIN)
                    .setComponent(cName)
                    .putExtra(PluginData.EXTRA_DATA_SENDER, packageName)
                displayNames[cName] = it.serviceInfo.loadLabel(packageManager).toString()
                if(!bindService(sbi, pluginConnector, BIND_AUTO_CREATE)){
                    Log.d("App", "Cannot bind ${it.serviceInfo.name}")
                }
            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }

    var shouldUpdate = false
    var startOnBoot : Boolean
        get() {
            val cmp = ComponentName(applicationContext, BootStartReceiver::class.java)
            val pm = applicationContext.packageManager
            return pm.getComponentEnabledSetting(cmp) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        set(value) {
            val cmp = ComponentName(applicationContext, BootStartReceiver::class.java)
            val pm = applicationContext.packageManager
            pm.setComponentEnabledSetting(
                cmp,
                value.select(
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                ), PackageManager.DONT_KILL_APP)
        }


    private var reReadPreference = false
    val activePlugins = arrayListOf<PluginId>()
    var defaultPlugin = PluginId("","")
    var hideOnTap = false
    lateinit var pref : SharedPreferences

    @SuppressLint("ApplySharedPref")
    private fun readPreferences(){
        updateFrequency = pref.getLong(PK_THREADED_UPDATE_FREQ, 1000L)

        if(!pref.contains(PK_ENABLED_PLUGIN)){
            val internalName = InternalStatProviderService::class.java.name
            val sb = StringBuilder()
            arrayListOf(
                PluginId(internalName,InternalStatProviderService.DAT_TEMP_CPU),
                PluginId(internalName,InternalStatProviderService.DAT_TEMP_BATTERY),
                PluginId(internalName,InternalStatProviderService.DAT_RAM_USAGE),
                PluginId(internalName,InternalStatProviderService.DAT_BATTERY_INFO),
                PluginId(internalName,InternalStatProviderService.DAT_NET_UPLOAD),
                PluginId(internalName,InternalStatProviderService.DAT_NET_DNLOAD)
            ).forEach {
                sb.append(it.toString()).append(',')
            }
            // We need the next command to run after the write finished
            pref.edit().putString(PK_ENABLED_PLUGIN, sb.toString()).commit()
        }
        val plugins = pref.getString(PK_ENABLED_PLUGIN,"") ?: ""
        activePlugins.clear()
        plugins.split(",").forEach {
            if(it.isNotEmpty()){
                activePlugins.add(PluginId(it))
            }
        }
        val defPlug = pref.getString(PK_DEFAULT_PLUGIN_DISPLAY, null)
        if(defPlug != null){
            defaultPlugin = PluginId(defPlug)
        }
        startOnBoot = pref.getBoolean(PK_RUN_ON_STARTUP, false)
        hideOnTap = pref.getBoolean(PK_HIDE_ON_TAP, false)
    }

    fun savePreferences(){
        val activePluginIds = StringBuilder()
        activePlugins.forEach { activePluginIds.append(it.toString()).append(',') }
        pref.edit()
            .putLong(PK_THREADED_UPDATE_FREQ, updateFrequency)
            .putString(PK_ENABLED_PLUGIN, activePluginIds.toString())
            .putString(PK_DEFAULT_PLUGIN_DISPLAY, defaultPlugin.toString())
            .putBoolean(PK_RUN_ON_STARTUP, startOnBoot)
            .putBoolean(PK_HIDE_ON_TAP, hideOnTap)
            .apply()
    }

    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        readPreferences()
        listPlugins()
        fixedRateTimer("pluginPoll", false, 1000L, 300L){
            pluginList.forEach { info ->
                info.updateData(ctx)
            }
        }

        fixedRateTimer("libPrefUpdater", false, 0L, 1000L ){
            if(reReadPreference){
                readPreferences()
            }
        }
    }

    private var onMemoryCleaning = ArrayList<(Int) -> Unit>()

    override fun onTrimMemory(level: Int) {
        onMemoryCleaning.forEach {
            it.invoke(level)
        }
        super.onTrimMemory(level)
    }

    override fun onTerminate() {
        savePreferences()
        keepRunning = false
        clearPlugins()
        Log.d("App", "FloatStat terminating...")
        super.onTerminate()
    }

    fun refreshPluginList() {
        listPlugins()
    }

    fun openDonateUri() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("https://github.com/EmiyaSyahriel/floatstat/blob/master/readme/DONATE.MD")
        startActivity(i)
    }

    fun clearPlugins() {
        iconProvider.clearMemFile()
        unbindService(pluginConnector)
        pluginList.forEach {
            it.binder.requestStop()
        }
    }

}