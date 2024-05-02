package id.psw.floatstat.providers

import id.psw.floatstat.plugins.PluginData

internal abstract class IProvider {
    abstract fun init()
    abstract fun getData() : PluginData
    abstract fun close()
    abstract fun updateData()
}