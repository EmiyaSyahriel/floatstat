package id.psw.floatstat

import android.content.Context
import android.os.Build
import kotlin.math.max

fun sdkAtLeast(ver: Int) : Boolean = Build.VERSION.SDK_INT >= ver

val Context.app : App get(){
    return if(this is App) this
    else this.applicationContext as App
}

fun <T> Boolean.select(a:T, b:T) : T = if(this) a else b

inline fun <T> Collection<T>.forEachLA(block: (T) -> Unit){
    val kSize = size
    var i  =0
    while(i < kSize)
    {
        block(elementAt(i))
        i++
    }
}

inline fun <T> Collection<T>.forEachIndexedLA(block: (Int, T) -> Unit){
    val kSize = size
    var i = 0
    while(i < kSize)
    {
        block(i, elementAt(i))
        i++
    }
}

inline fun <T> Collection<T>.firstOrNullLA(block: (T) -> Boolean) : T?
{
    val kSize = size
    var i = 0
    while(i < kSize) {
        if (block(elementAt(i))){
            return elementAt(i)
        }
        i++
    }
    return null
}

inline fun <T> Array<T>.forEachLA(block: (T) -> Unit){
    val kSize = size
    var i  =0
    while(i < kSize)
    {
        block(elementAt(i))
        i++
    }
}

inline fun <T> Array<T>.forEachIndexedLA(block: (Int, T) -> Unit){
    val kSize = size
    var i = 0
    while(i < kSize)
    {
        block(i, elementAt(i))
        i++
    }
}

inline fun <T> Array<T>.firstOrNullLA(block: (T) -> Boolean) : T?
{
    val kSize = size
    var i = 0
    while(i < kSize) {
        if (block(elementAt(i))){
            return elementAt(i)
        }
        i++
    }
    return null
}

inline fun <K, V> Map<K,V>.forEachLA (block : (K,V) -> Unit) {
    val kSize = max(keys.size, values.size)
    var i = 0
    while(i < kSize) {
        val k = keys.elementAt(i)
        val v = values.elementAt(i)
        block(k,v)
        i++
    }
}