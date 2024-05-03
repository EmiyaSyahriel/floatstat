package id.psw.floatstat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import kotlin.concurrent.timer

class TileLongClickActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!app.isFloatServiceRunning){
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(app, "Starting service", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed ({
                startEditor()
            }, 3000)
        }else{
            startEditor()
        }
        finish()
    }

    private fun startEditor(){
        startService(Intent(FloatWindowService.ACTION_EDIT).apply {
            this.setClass(this@TileLongClickActivity, FloatWindowService::class.java)
        })
    }
}