package id.psw.floatstat

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.os.*
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.graphics.minus
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.psw.floatstat.views.PluginSelectorAdapter
import id.psw.floatstat.views.PluginSelectorItem
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.concurrent.timer
import kotlin.system.exitProcess

class FloatWindowService : Service() {

    companion object {
        const val NOTIF_ID = "_psw_temperamon_"
        const val NOTIF_INT_ID = 8914
        const val INT_ACTION_VISIBILITY = 1098
        const val INT_ACTION_EDIT = 1099
        const val INT_ACTION_CLOSE = 1100
        const val INT_ACTION_EXPAND = 1101
        const val ACTION_VISIBILITY = "id.psw.temperamon.action.VISIBILITY"
        const val ACTION_EDIT = "id.psw.temperamon.action.EDIT"
        const val ACTION_CLOSE = "id.psw.temperamon.action.CLOSE"
        const val ACTION_EXPAND = "id.psw.temperamon.action.EXPAND"
        private const val allowEditor: Boolean = true

        fun startServiceS(ctx: Context){
            val i = Intent(ctx, FloatWindowService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(i)
            }else{
                ctx.startService(i)
            }
        }
    }

    private var vMainView : StatusView? = null
    private var vWinMan : WindowManager? = null
    private var vNotifMan : NotificationManager? = null
    private var vNotif : Notification? = null
    private lateinit var viewParam :  WindowManager.LayoutParams
    private val currentTouch = PointF()
    private var slop = 10
    private var w = 0
    private var h = 0

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createView(){
        val mainView = StatusView(this)

        slop = ViewConfiguration.get(this).scaledTouchSlop
        val viewType = if(sdkAtLeast(Build.VERSION_CODES.O))
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        w = resources.displayMetrics.widthPixels / 2
        h = resources.displayMetrics.heightPixels / 2

        viewParam = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            viewType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT
        ).apply {
            x = -w
        }
        vMainView = mainView
        val winMan = getSystemService(WINDOW_SERVICE) as WindowManager
        winMan.addView(mainView, viewParam)
        mainView.x = 0f
        mainView.y = 0f
        vWinMan = winMan

        mainView.setOnClickListener {
            mainView.isExpanded = !mainView.isExpanded
            app().shouldUpdate = true
        }

        mainView.setOnTouchListener { _, e ->
            when(e.action){
                MotionEvent.ACTION_DOWN -> {
                    mainView.isDragged = true
                    mainView.initialTouch.set(e.rawX, e.rawY)
                    mainView.initialPosition.set(viewParam.x, viewParam.y)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mainView.isDragged = false
                    currentTouch.set(e.rawX, e.rawY)

                    w = resources.displayMetrics.widthPixels / 2
                    viewParam.x = if(viewParam.x < 0) -w else w

                    if((currentTouch - mainView.initialTouch).length() < slop){ // Is Tap
                        mainView.isExpanded = !mainView.isExpanded
                        app().shouldUpdate = true

                        val sz = mainView.getSize()
                        viewParam.width = sz.x
                        viewParam.height = sz.y
                    }
                    winMan.updateViewLayout(mainView, viewParam)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if(mainView.isDragged){
                        // for some reason, (0,0) is right at the center
                        w = resources.displayMetrics.widthPixels / 2
                        h = resources.displayMetrics.heightPixels / 2
                        val x = (mainView.initialPosition.x + (e.rawX - mainView.initialTouch.x)).toInt().coerceIn(-w, w)
                        val y = (mainView.initialPosition.y + (e.rawY - mainView.initialTouch.y)).toInt().coerceIn(-h, h)
                        viewParam.x = x
                        viewParam.y = y
                        winMan.updateViewLayout(mainView, viewParam)
                    }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("LaunchActivityFromNotification") // We are only
    private fun createNotification(){
        val notifMan = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifChan = NotificationChannel(NOTIF_ID, getString(R.string.notif_channel_id), NotificationManager.IMPORTANCE_LOW)
            notifMan.createNotificationChannel(notifChan)
        }
        val notifFlag = if(sdkAtLeast(Build.VERSION_CODES.M)) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else 0

        val notif = NotificationCompat.Builder(this, NOTIF_ID)
            .setContentTitle(getString(R.string.float_stat_notif_title))
            .setContentText(getString(R.string.float_stat_notif_desc))
            .setSmallIcon(R.drawable.ic_main_notification)
            .setSilent(true)
            .setContentIntent(PendingIntent.getService(this, INT_ACTION_EXPAND,
                    Intent(ACTION_EXPAND), notifFlag))
            .addAction(android.R.drawable.ic_menu_view, getString(R.string.float_stat_notif_visibility),
                PendingIntent.getService(this, INT_ACTION_VISIBILITY,
                    Intent(ACTION_VISIBILITY), notifFlag))
            .addAction(android.R.drawable.ic_menu_edit, getString(R.string.float_stat_notif_edit),
                PendingIntent.getService(this, INT_ACTION_EDIT,
                    Intent(ACTION_EDIT), notifFlag))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.float_stat_notif_close),
                PendingIntent.getService(this, INT_ACTION_CLOSE,
                    Intent(ACTION_CLOSE), notifFlag))
            .build()
        vNotif = notif
        notif.flags = Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        notifMan.notify(NOTIF_INT_ID, notif)
        vNotifMan = notifMan

        startForeground(NOTIF_INT_ID, notif)
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
        createView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_CLOSE -> {
                stopSelf()
                exitProcess(0)
            }
            ACTION_EDIT -> {
                openEditWindow()
            }
            ACTION_VISIBILITY -> {
                vMainView?.visibility = when(vMainView?.visibility){
                    View.VISIBLE -> View.GONE
                    View.GONE -> View.VISIBLE
                    else -> View.VISIBLE
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var editWindow : AlertDialog? = null
    private val wHandler = Handler(Looper.myLooper()!!)

    private fun openEditWindow() {

        val isDark =
            (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val ic = if (sdkAtLeast(Build.VERSION_CODES.LOLLIPOP)) {
            isDark.select(android.R.style.Theme_Material_Dialog_Alert, android.R.style.Theme_Material_Light_Dialog_Alert)
        } else {
            isDark.select(AlertDialog.THEME_DEVICE_DEFAULT_DARK, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        }

        val app = app()
        applicationContext.setTheme(ic)
        val dlgBuilder = AlertDialog.Builder(applicationContext, ic)
        var selectorRView : RecyclerView? = null
        if(allowEditor){
            selectorRView = RecyclerView(applicationContext)
            val dataList = arrayListOf<PluginSelectorItem>()

            app.pluginList.forEach { pg ->
                pg.dataList.forEach { dt ->
                    val pDspName = pg.displayName
                    val dDspName = dt.displayName
                    val enabled = app.activePlugins.firstOrNull { aPlug -> aPlug.equals(pg.name.className, dt.id) } != null
                    dataList.add(PluginSelectorItem(pg.name.className, pDspName, dt.id, dDspName, enabled))
                }
            }

            dataList.sortBy {  dtl ->
                val idx = app.activePlugins.indexOfFirst { it.equals(dtl.pkgName, dtl.id) }
                (idx == -1).select(Int.MAX_VALUE, idx)
            } // Sort by Active and Order, Inactive will be placed at bottom

            val adapter = PluginSelectorAdapter(applicationContext, dataList)
            selectorRView.adapter = adapter
            adapter.selectedDefault = dataList.indexOfFirst { app.defaultPlugin.equals(it.pkgName, it.id) }
            selectorRView.layoutManager = LinearLayoutManager(applicationContext)

            val titleBar = LayoutInflater.from(applicationContext).inflate(R.layout.plugin_selector_title, null)
            val cfColor = isDark.select(Color.WHITE, Color.BLACK)
            titleBar.findViewById<ImageButton>(R.id.menu_btn).apply {
                colorFilter = PorterDuffColorFilter(cfColor, PorterDuff.Mode.SRC_IN)
                setOnClickListener { btn ->
                    PopupMenu(context, this).apply {
                        menuInflater.inflate(R.menu.selector_menu, menu)
                        menu.findItem(R.id.plugin_selector_bootstart).isChecked = app.startOnBoot

                        setOnMenuItemClickListener {
                            when(it.itemId){
                                R.id.plugin_selector_donate -> { app().openDonateUri() }
                                R.id.plugin_selector_refresh -> {
                                    editWindow?.cancel()
                                    app().refreshPluginList()
                                    Toast.makeText(app, context.getString(R.string.plugin_selector_updating_warning), Toast.LENGTH_LONG).show()
                                    Timer("UpdateWait", false).schedule(1000){
                                        wHandler.post {
                                            openEditWindow()
                                        }
                                    }
                                }
                                R.id.plugin_selector_bootstart -> {
                                    app.startOnBoot = !app.startOnBoot
                                    app.savePreferences()
                                }
                            }
                            true
                        }
                        show()
                    }
                }
            }
            titleBar.findViewById<TextView>(R.id.title).apply {
                setTextColor(cfColor)
            }
            dlgBuilder.setCustomTitle(titleBar)
                .setView(selectorRView)
                .setPositiveButton(android.R.string.ok){
                    dlg, _ ->
                    saveOrderAndActivation(dataList, adapter.selectedDefault)
                    dlg.dismiss()
                }
                .setNegativeButton(android.R.string.cancel){
                    dlg, _ -> dlg.cancel()
                }
        }else{
            dlgBuilder.setTitle("In Progress")
                .setMessage("Editing not yet available")
                .setNegativeButton(android.R.string.ok){
                        dlg, _ ->
                    dlg.cancel()
                }
        }
        val dlg = dlgBuilder.create()
        editWindow = dlg
        if (sdkAtLeast(Build.VERSION_CODES.O)) {
            dlg.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }else{
            dlg.window?.setType(WindowManager.LayoutParams.TYPE_PHONE)
        }
        dlg.show()
    }

    private fun saveOrderAndActivation(dataList: ArrayList<PluginSelectorItem>, selectedDefault: Int) {
        val app = app()
        app.activePlugins.clear()
        dataList.filter { it.isActive }.forEach {
            app.activePlugins.add(App.PluginId(it.pkgName, it.id))
        }
        if(selectedDefault < dataList.size && selectedDefault >= 0){
            val def = dataList[selectedDefault]
            app.defaultPlugin = App.PluginId(def.pkgName, def.id)
        }
        app.savePreferences()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val lastH = h
        h = resources.displayMetrics.heightPixels / 2
        w = resources.displayMetrics.widthPixels / 2
        val y = ((viewParam.y.toFloat() / lastH) * h).toInt()
        val x = (viewParam.x < 0).select(-w, w)
        viewParam.x = x
        viewParam.y = y
        vWinMan?.updateViewLayout(vMainView, viewParam)
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(applicationContext, "Closing View", Toast.LENGTH_SHORT).show()
        if(vMainView != null){
            vWinMan?.removeView(vMainView)
        }
        vNotifMan?.cancel(NOTIF_INT_ID)
    }

}