package id.psw.floatstat.plugins

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor

abstract class PluginIconProvider : ContentProvider() {
    override fun query(uri: Uri, projection: Array<out String>?,
        selection: String?, selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri?  = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri, values: ContentValues?,
        selection: String?, selectionArgs: Array<out String>?
    ): Int = 0

    open var allowWrite = false

    abstract fun onRequestFile(path:List<String>?) : ParcelFileDescriptor?
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        if(mode.contains('w', false) && !allowWrite) return null
        return onRequestFile(uri.pathSegments)
    }
}