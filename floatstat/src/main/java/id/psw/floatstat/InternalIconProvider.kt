package id.psw.floatstat

import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import id.psw.floatstat.plugins.PluginIconProvider
import java.io.File

class InternalIconProvider : PluginIconProvider() {
    companion object {
        private const val MEMORY_FILE_NAME = "n61plus"
        private const val CONTENT_DIR = "ne63_content"

    }

    private val files = mutableMapOf<Int, File>()
    private lateinit var cacheDir : File

    override fun onCreate(): Boolean {
        val app = context!!.app()

        val cDir = app.externalCacheDir ?: app.cacheDir
        cacheDir = File(cDir, "icon_caches")
        if(cacheDir.exists()){
            cacheDir.deleteRecursively()
            cacheDir.mkdir()
        }

        app.iconProvider = this
        return true
    }

    override fun getType(uri: Uri): String = "image/png"

    fun clearMemFile(){
        files.clear()
    }

    fun registerIcon(id:Int, bm: Bitmap) : Uri{
        val fileName = "${MEMORY_FILE_NAME}_$id"
        if(!cacheDir.exists()){
            cacheDir.mkdir()
        }

        val file = File.createTempFile(fileName, ".png", cacheDir)
        file.deleteOnExit()
        files[id] = file
        bm.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
        return Uri.parse("content://${javaClass.name}/${CONTENT_DIR}/${fileName}")
    }

    override fun onRequestFile(path: List<String>?): ParcelFileDescriptor? {
        if(path != null) {
            if (path.size >= 2) {
                if (path[0] == CONTENT_DIR) {
                    val fileNames = path[1].split('_')
                    if (fileNames.size >= 2) {
                        val id = fileNames[1].toIntOrNull() ?: -1;
                        if (fileNames[0] == MEMORY_FILE_NAME &&
                            files.containsKey(id)
                        ) {
                            return ParcelFileDescriptor.open(files[id], ParcelFileDescriptor.MODE_READ_ONLY)
                        }else{
                            return null
                        }
                    }
                }
            }
        }
        return null
    }

}