package id.psw.floatstat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation

class StatusView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attributeSet, defStyleAttr) {

    companion object{
        private var LINE_HEIGHT = 20.0f
        private const val PADDING = 5.0f
    }

    // TODO: use plugin instead of static data
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 15.0f
        typeface = Typeface.DEFAULT
    }
    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(128,0,0,0)
    }
    var isExpanded = true
    private val boundRectF = RectF(0f, 0f, 0f, 0f)
    private val clipRectF =  RectF(0f, 0f, 0f, 0f)
    private val location = PointF()
    private val drawRectF = RectF()
    var isDragged = false
    val initialPosition = Point()
    val initialTouch = PointF()
    private val app = context.app

    private fun si(i: Int) : Int = (resources.displayMetrics.density * i).toInt()
    private fun sf(i: Float) : Int = (resources.displayMetrics.density * i).toInt()

    private val updaterThread = Thread{
        while(true){
            if(app.shouldUpdate){
                postInvalidate()
            }
            Thread.sleep(20)
        }
    }.apply { name = "updater.thr" }

    init {
        updaterThread.start()
    }

    private var maxWidth = 20.0f

    private fun Canvas.drawText(text:String, x:Float, y:Float, paint:Paint, yOffset:Float){
        drawText(text, x, y + (paint.textSize * (1.0f - yOffset)), paint)
    }
    private val tmpPoint = Point()

    fun getSize(scaled : Boolean=true) :Point {
        val pgCount = app.activePlugins.size
        val count = isExpanded.select(pgCount, 1)
        val icon = (PADDING * 3.0f + LINE_HEIGHT)
        val h = ((PADDING * 2) + (count * LINE_HEIGHT)).toInt()
        val w = isExpanded.select(icon + maxWidth, icon)
        if(scaled)
            tmpPoint.set(sf(w), si(h))
        else
            tmpPoint.set(w.toInt(),h)

        return tmpPoint
    }

    private fun getMaxPluginDataValueWidth(){
        var maxWidth = 20.0f
        app.pluginList.forEach { plug ->
            plug.dataList.forEach {
                val w = textPaint.measureText(it.value)
                if(maxWidth < w) maxWidth = w
            }
        }
        this.maxWidth = maxWidth + textPaint.textSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        getMaxPluginDataValueWidth()
        val sz = getSize()
        val w = MeasureSpec.makeMeasureSpec(sz.x, MeasureSpec.EXACTLY)
        val h = MeasureSpec.makeMeasureSpec(sz.y + 5, MeasureSpec.EXACTLY)
        super.onMeasure(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        getMaxPluginDataValueWidth()
        val sz = getSize()
        super.onSizeChanged(
            sz.x,
            sz.y,
            oldw, oldh)
    }

    private fun updateLayout() {
        getMaxPluginDataValueWidth()
        val sz = getSize()
        if(isExpanded){
            layout(
                x.toInt(),
                y.toInt(),
                (x + sz.x).toInt(),
                (y + sz.y).toInt()
            )
        }else{
            layout(x.toInt(),y.toInt(),x.toInt()+30,y.toInt()+30)
        }
    }

    private fun onDrawCollapsed(ctx:Canvas){
        drawPaint.colorFilter = null
        drawPaint.color = Color.argb(128,0,0,0)
        ctx.drawCircle(15.0f, 15.0f, 15.0f, drawPaint)
        textPaint.textAlign = Paint.Align.CENTER
        var text = ""
        app.pluginList.forEach { plug ->
            plug.dataList.forEach { dat ->
                if(app.defaultPlugin.equals(plug.name.className, dat.id)){
                    text = dat.value
                    textPaint.color = dat.textTint
                }
            }
        }

        ctx.drawText(text, 15.0f, 13.0f, textPaint, 0.5f)
        textPaint.color = Color.WHITE
    }

    private fun onDrawExpanded(ctx:Canvas) {
        drawPaint.colorFilter = null
        drawPaint.color = Color.argb(128, 0, 0, 0)
        ctx.drawRoundRect(boundRectF, 10.0f, 10.0f, drawPaint)
        textPaint.textAlign = Paint.Align.LEFT
        val lineSz = (textPaint.textSize * 1.25f)
        LINE_HEIGHT = lineSz
        val icLeft = PADDING * 1.0f
        val txLeft = (PADDING * 2.0f) + lineSz
        val app = context.app
        app.activePlugins.forEachIndexed { i, aPlug ->
            app.pluginList.firstOrNull {
                    plug -> plug.name.className == aPlug.pkg
            }?.also {
                    plug -> plug.dataList.firstOrNull { dat -> dat.id == aPlug.id }
                ?.also {
                    val icTop = PADDING + (i * lineSz)
                    val icon = it.icon
                    if(icon != null && !icon.isRecycled){
                        drawPaint.color = it.iconTint
                        if(it.iconTint == Color.TRANSPARENT){
                            drawPaint.colorFilter = null
                        }else{
                            drawPaint.colorFilter = PorterDuffColorFilter(it.iconTint, PorterDuff.Mode.SRC_IN)
                        }
                        drawRectF.set(
                            icLeft, icTop,
                            icLeft + lineSz, icTop + lineSz
                        )
                        ctx.drawBitmap(icon, null, drawRectF, drawPaint)
                    }
                    textPaint.color = it.textTint
                    ctx.drawText(it.value, txLeft, icTop + textPaint.textSize, textPaint)
                }
            }
        }
    }

    override fun onDraw(ctx: Canvas) {
        val pref = resources.displayMetrics.density

        getMaxPluginDataValueWidth()
        val sz = getSize(false)
        boundRectF.set(0.0f, 0.0f, sz.x.toFloat(), sz.y.toFloat())

        ctx.withTranslation(location.x, location.y) {
            ctx.withScale(pref, pref){
                if(isExpanded){
                    clipRectF.set(boundRectF)
                }else{
                    clipRectF.set(0.0f, 0.0f, 30.0f, 30.0f)
                }
                withClip(clipRectF){
                    if(isExpanded) {
                        onDrawExpanded(ctx)
                    }else{
                        onDrawCollapsed(ctx)
                    }
                }
            }
        }
        updateLayout()
        super.onDraw(ctx)
    }

}