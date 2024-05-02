package id.psw.floatstat.plugin_example.providers

import id.psw.floatstat.plugin_example.SamplePluginService
import id.psw.floatstat.plugins.PluginData

open class PluginDataProvider(val ctx: SamplePluginService) {
    open fun getData() : PluginData = SamplePluginService.blankPluginData
}