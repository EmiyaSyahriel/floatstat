package id.psw.floatstat.providers

import android.graphics.Color
import id.psw.floatstat.InternalStatProviderService
import id.psw.floatstat.plugins.PluginData
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception

internal class CpuTempProvider(val ctx:InternalStatProviderService) : IProvider(){
    @Volatile private var shouldUpdate = true
    @Volatile private var tSenseSensorTemp = Float.NaN

    override fun updateData(){
        val thermalDir = File("/sys/class/thermal")
        val tempData = mutableMapOf<String, Float>()
        val cpuTempData = arrayListOf<Float>()

        thermalDir.list { dir, _ -> dir.isDirectory }?.forEach {
            try{
                val ls = File(thermalDir, it)
                val typeFile = File(ls, "type")
                val tempFile = File(ls, "temp")
                if(typeFile.exists() && tempFile.exists()){
                    val typeRac = RandomAccessFile(typeFile, "r")
                    val tempRac = RandomAccessFile(tempFile, "r")
                    val type = typeRac.readLine().trim()
                    val temp = tempRac.readLine().trim()
                    typeRac.close()
                    tempRac.close()
                    val scale = when(temp.length){
                        1, 2 -> 1.0f
                        3 -> 10.0f
                        4 -> 100.0f
                        5 -> 1000.0f
                        else -> 1.0f
                    }
                    tempData[type.trim()] = temp.toFloat() / scale
                }

            }catch(_: Exception){}
        }
        cpuTempData.clear()
        tempData.forEach {
            if(it.key.contains("tz")){
                cpuTempData.add(it.value)
            }
        }
        tSenseSensorTemp = if(cpuTempData.size > 0){
            cpuTempData.average().toFloat()
        }else{
            Float.NaN
        }
    }

    private val dat = PluginData()
    private var shouldReset = true

    override fun init() {
        dat.iconColor.update(Color.WHITE)
        dat.iconValue.update(ctx.iconUris[InternalStatProviderService.IC_CPU_TEMP]!!)
        shouldReset = false
    }

    override fun getData(): PluginData {
        if(shouldReset) dat.reset()
        shouldReset = true

        if(shouldUpdate){
            val temp = tSenseSensorTemp
            var tempStr = "???"
            var color = Color.GRAY
            if(tSenseSensorTemp.isFinite()){
                tempStr = "${"%.1f".format(temp)}${InternalStatProviderService.degree}"
                color = ctx.tempColor(tSenseSensorTemp - 10.0f)
            }
            dat.textValue.update(tempStr)
            dat.textColor.update(color)
            dat.iconColor.update(color)
            shouldUpdate = false
        }

        return dat
    }

    override fun close() {
    }
}