package id.psw.floatstat.plugin_example

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.FileProvider
import id.psw.floatstat.plugins.PluginIconProvider
import java.io.File

class IconProvider : PluginIconProvider() {
    companion object {
        private const val TAG = "IconProvider"
        private val IntGetter = Regex("[0-9]+")
        fun createIconUri(resId: Int) : Uri =
            Uri.parse("content://${IconProvider::class.java.name}/icon/$resId")
        fun getResFromUri(uri:List<String>) : Int {
            val ui = IntGetter.find(uri.last(), 0)
            if(ui != null){
                return ui.value.toIntOrNull() ?: -1
            }
            return -1
        }
    }

    private val fileName = mutableMapOf<Int, File>()

    private fun writeBitmapToFile(resId:Int, cacheDir:File?){
        if(fileName.containsKey(resId)) return
        val outFile = File.createTempFile("tmp", ".png", cacheDir)
        outFile.deleteOnExit()
        Log.d(TAG, "Saving resource {$resId} as file to ${outFile.absolutePath}")
        val d = context!!.resources.getDrawable(resId)
        val bm = Bitmap.createBitmap(d.intrinsicWidth,d.intrinsicHeight, Bitmap.Config.ALPHA_8)
        val cnv = Canvas(bm)
        d.setBounds(0,0,d.intrinsicWidth, d.intrinsicHeight)
        d.draw(cnv)
        if(bm.compress(Bitmap.CompressFormat.PNG, 100, outFile.outputStream())){
            fileName[resId] = outFile
        }
        bm.recycle() // Remove from memory
    }

    override fun onCreate(): Boolean {
        var cacheDir = context?.externalCacheDir ?: context?.cacheDir
        if(cacheDir != null){
            cacheDir = File(cacheDir, "_icon")
            if(!cacheDir.exists()) cacheDir.deleteRecursively()
            cacheDir.mkdir()
        }
        (context!!.applicationContext as PluginApp).iconProvider = this
        arrayOf(
            R.drawable.ic_plugin_icon,
            R.drawable.ic_plugin_rng,
            R.drawable.ic_plugin_clock,
        ).forEach {
            writeBitmapToFile(it,cacheDir)
        }
        return true
    }

    override fun getType(uri: Uri): String { return "image/png" }
    override fun onRequestFile(path: List<String>?): ParcelFileDescriptor? {
        if(path != null) {
            val resId = getResFromUri(path)
            if (fileName.containsKey(resId)) {
                return ParcelFileDescriptor.open(
                    fileName[resId],
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
            }
        }
        return null
    }
}