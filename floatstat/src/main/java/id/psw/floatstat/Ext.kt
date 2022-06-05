package id.psw.floatstat

import android.content.Context
import android.os.Build

fun sdkAtLeast(ver: Int) : Boolean = Build.VERSION.SDK_INT >= ver
fun sdkGreaterThan(ver: Int) : Boolean = Build.VERSION.SDK_INT > ver

fun Context.app() : App {
    return if(this is App) this
    else this.applicationContext as App
}

fun <T> Boolean.select(a:T, b:T) : T = if(this) a else b