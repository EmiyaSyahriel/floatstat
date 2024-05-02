package id.psw.floatstat.plugin_example.impls

import id.psw.floatstat.IFloatStatDataPlugin
import id.psw.floatstat.plugin_example.R
import id.psw.floatstat.plugin_example.SamplePluginService
import id.psw.floatstat.plugins.PluginData

class SamplePluginImpl(private val context: SamplePluginService) : IFloatStatDataPlugin {

    override val dataIds : String get(){
        val sb = StringBuilder()
        arrayOf(SamplePluginService.PLUGIN_NAME_1, SamplePluginService.PLUGIN_NAME_2).apply {
            forEachIndexed { i, it ->
                sb.append(it)
                if(i < size-1) sb.append(',')
            }
        }
        return sb.toString()
    }
    override fun getDataName(dataId: String): String {
        return when(dataId){
            SamplePluginService.PLUGIN_NAME_1 -> { context.getString(R.string.plugin_name_rng) }
            SamplePluginService.PLUGIN_NAME_2 -> { context.getString(R.string.plugin_name_clock) }
            else -> { context.getString(R.string.unknown_data_name) }
        }
    }

    override fun getData(dataId: String): PluginData {
        return when(dataId){
            SamplePluginService.PLUGIN_NAME_1 -> context.rng.getData()
            SamplePluginService.PLUGIN_NAME_2 ->  context.clock.getData()
            else -> SamplePluginService.blankPluginData
        }
    }

    override fun requestStop() {
        context.stopSelf()
    }
}