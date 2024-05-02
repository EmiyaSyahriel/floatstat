package id.psw.floatstat

import id.psw.floatstat.plugins.PluginData
import remoter.annotations.Remoter

@Remoter
interface IFloatStatDataPlugin {
    val dataIds: String
    fun getDataName(dataId: String): String
    fun getData(dataId: String): PluginData
    fun requestStop()
}