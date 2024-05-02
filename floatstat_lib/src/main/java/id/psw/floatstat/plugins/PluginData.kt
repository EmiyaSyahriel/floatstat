package id.psw.floatstat.plugins

import android.graphics.Color
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.nio.ByteBuffer

class PluginData() : Parcelable {
    private infix fun Byte.hasFlag(b:Byte) : Boolean = this.toInt() and b.toInt() == b.toInt()
    private infix fun Byte.addFlag(b:Byte) : Byte = (this.toInt() or b.toInt()).toByte()
    data class PluginDataAtom <T> (var updated : Boolean, var value : T){
        fun update(v : T){
            value = v
            updated = true
        }
    }

    var textValue = PluginDataAtom(false, "")
    var textColor = PluginDataAtom(false, Color.WHITE)
    var iconValue = PluginDataAtom(false, Uri.parse("about:blank"))
    var iconColor = PluginDataAtom(false, Color.WHITE)

    constructor(parcel: Parcel) : this() {
        val flag = parcel.readByte()
        textValue.updated = flag hasFlag FLAG_TEXT_VALUE_UPDATE
        textColor.updated = flag hasFlag FLAG_TEXT_COLOR_UPDATE
        iconValue.updated = flag hasFlag FLAG_ICON_VALUE_UPDATE
        iconColor.updated = flag hasFlag FLAG_ICON_COLOR_UPDATE
        if(textValue.updated) textValue.value = readPrefixedString(parcel)
        if(textColor.updated) textColor.value = parcel.readInt()
        if(iconValue.updated) iconValue.value = Uri.parse(readPrefixedString(parcel))
        if(iconColor.updated) iconColor.value = parcel.readInt()
    }

    @Suppress("KotlinConstantConditions") // Allow the code to be more comprehensible!
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        var flag : Byte = 0
        if(textValue.updated) flag = flag addFlag FLAG_TEXT_VALUE_UPDATE
        if(textColor.updated) flag = flag addFlag FLAG_TEXT_COLOR_UPDATE
        if(iconValue.updated) flag = flag addFlag FLAG_ICON_VALUE_UPDATE
        if(iconColor.updated) flag = flag addFlag FLAG_ICON_COLOR_UPDATE
        parcel.writeByte(flag)
        if(textValue.updated) writePrefixedString(parcel, textValue.value)
        if(textColor.updated) parcel.writeInt(textColor.value)
        if(iconValue.updated) writePrefixedString(parcel, iconValue.value.toString())
        if(iconColor.updated) parcel.writeInt(iconColor.value)
    }

    override fun describeContents(): Int = 0

    companion object {
        const val FLAG_TEXT_VALUE_UPDATE : Byte = 0b00000001
        const val FLAG_TEXT_COLOR_UPDATE : Byte = 0b00000010
        const val FLAG_ICON_VALUE_UPDATE : Byte = 0b00000100
        const val FLAG_ICON_COLOR_UPDATE : Byte = 0b00001000
        /**
         * Contains package name of the sender service
         * can be used to grant uri permission for this package
         * or other thing
         */
        const val EXTRA_DATA_SENDER = "misi_gosen.paket_dari"

        @JvmField
        val CREATOR = object : Parcelable.Creator<PluginData> {
            override fun createFromParcel(parcel: Parcel): PluginData {
                return PluginData(parcel)
            }

            override fun newArray(size: Int): Array<PluginData?> {
                return arrayOfNulls(size)
            }
        }

        fun readPrefixedString(parcel : Parcel) : String{
            val strLen = parcel.readInt()
            val ba = ByteArray(strLen)
            parcel.readByteArray(ba)
            val bb = ByteBuffer.wrap(ba)
            val cb = Charsets.UTF_16LE.decode(bb)
            return cb.toString()
        }

        fun writePrefixedString(parcel : Parcel, string : String){
            val arr = Charsets.UTF_16LE.encode(string).array()
            parcel.writeInt(arr.size)
            parcel.writeByteArray(arr, 0, arr.size)
        }
    }
}