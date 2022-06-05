package id.psw.floatstat

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("id.psw.floatstat", appContext.packageName)
    }

    @Test
    fun readTemperatureSensor(){

    }

    @Test
    fun readTemperature(){
        val thermalDir = File("/sys/class/thermal")
        thermalDir.list { dir, _ -> dir.isDirectory }?.forEach {
            try{
                val ls = File(thermalDir, it)
                val typeFile = File(ls, "type")
                val tempFile = File(ls, "temp")
                if(typeFile.exists() && tempFile.exists()){
                    val typeRac = RandomAccessFile(typeFile, "r")
                    val tempRac = RandomAccessFile(tempFile, "r")
                    val type = typeRac.readLine()
                    val temp = tempRac.readLine()
                    typeRac.close()
                    tempRac.close()
                    Log.d("ReadTemp","$it -> $type : $temp")
                }
            }catch(e:Exception){
                Log.e("ReadTemp","$it -> ${e.javaClass.name} : ${e.message}")
            }
        }
    }
}