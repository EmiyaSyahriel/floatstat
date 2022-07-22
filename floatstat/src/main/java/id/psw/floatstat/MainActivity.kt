package id.psw.floatstat

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class MainActivity : Activity() {
    companion object{
        const val SYSTEM_ALERT_PERMISSION = 1928
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No need to setup view, this activity will finish anyway
        if(isPermitted()){
            startServices()
        }else{
            askPermission()
        }
        finish()
    }

    private fun startServices(){
        Toast.makeText(applicationContext, getString(R.string.temperamon_start), Toast.LENGTH_LONG).show()
        startWindowService()
    }

    private fun startWindowService() {
        FloatWindowService.startServiceS(applicationContext)
    }

    private fun askPermission() {
        if(sdkAtLeast(Build.VERSION_CODES.M)){
            val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            Toast.makeText(applicationContext, getString(R.string.allow_please), Toast.LENGTH_LONG).show()
            startActivityForResult(i, SYSTEM_ALERT_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(resultCode){
            SYSTEM_ALERT_PERMISSION -> {
                if(resultCode == RESULT_OK) {
                    startServices()
                }else{
                    Toast.makeText(applicationContext, getString(R.string.err_not_yet_allowed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun isPermitted(): Boolean {
        var permitted= true
        if(sdkAtLeast(Build.VERSION_CODES.M)){
            permitted = Settings.canDrawOverlays(this)
        }
        return permitted
    }
}