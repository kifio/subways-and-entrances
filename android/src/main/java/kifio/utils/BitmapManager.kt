package kifio.utils

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import kifio.App
import kifio.R

/**
 * Class which used for creating and caching Bitmaps markers.
 */
class BitmapManager() {

    private val bitmaps = mutableMapOf<String, Bitmap>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textRect = Rect()
    private val entranceColor = Color.parseColor("#FDB913")

    fun getIcons() = bitmaps

    fun getIcon(key: String) = bitmaps[key]

    fun hasIcon(key: String): Boolean {
        return bitmaps[key] != null
    }

    fun addEntranceIcon(number: String) {
        if (bitmaps[number] == null) {
            bitmaps[number] = buildEntranceLogo(number.toInt(), ENTRANCE_SIZE.toPx())
        }
    }

    fun addStationIcon(color: String) {
        if (bitmaps[color] == null) {
            bitmaps[color] = buildStationLogo(Color.parseColor(color), STATION_SIZE.toPx())
        }
    }

    private fun buildEntranceLogo(number: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawEntranceBackground(size.toFloat(), canvas)
        drawEntranceNumber(number.toString(), size.toFloat(), canvas)
        return bitmap
    }

    private fun drawEntranceBackground(size: Float, canvas: Canvas) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(0f, 0f, size, size),
                ENTRANCE_ICON_CORNER.toPx(),
                ENTRANCE_ICON_CORNER.toPx(),
                paint)
    }

    private fun drawEntranceNumber(number: String, size: Float, canvas: Canvas) {
        paint.color = entranceColor
        paint.strokeWidth = 0.5f.toPx()
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.textSize = 20f.toPx()
        paint.getTextBounds(number,0, number.length, textRect)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(number, size / 2, size - ((size - textRect.height()) / 2), paint)
    }

    private fun buildStationLogo(color: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawBackground(color, size, canvas)
        drawRoundedBitmapDrawable(bitmap, canvas)
        drawLogo(canvas)
        return bitmap
    }

    private fun drawBackground(color: Int, size: Int, canvas: Canvas) {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(color)
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setStroke(STROKE_WIDTH.toPx(), Color.WHITE)
        gradientDrawable.setBounds(0, 0, size, size)
        gradientDrawable.draw(canvas)
    }

    private fun drawRoundedBitmapDrawable(bitmap: Bitmap, canvas: Canvas) {
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(App.instance.resources, bitmap)
        roundedBitmapDrawable.setAntiAlias(true)
        roundedBitmapDrawable.cornerRadius = SUBWAY_ICON_CORNER
        roundedBitmapDrawable.draw(canvas)
    }

    private fun drawLogo(canvas: Canvas) {
        val drawable = ContextCompat.getDrawable(App.instance, R.drawable.metro_white) ?: return
        canvas.drawBitmap(fromDrawable(drawable, LOGO_SIZE.toInt().toPx()),
                ((STATION_SIZE - LOGO_SIZE) / 2).toPx(), ((STATION_SIZE - LOGO_SIZE) / 2).toPx(), null)
    }

    private fun fromDrawable(drawable: Drawable, size: Int): Bitmap {
        val width = if (size > 0) size else drawable.intrinsicWidth
        val height = if (size > 0) size else drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val STROKE_WIDTH = 2
        private const val SUBWAY_ICON_CORNER = 10f
        private const val ENTRANCE_ICON_CORNER = 2f
        private const val STATION_SIZE = 40
        private const val LOGO_SIZE = 24f
        private const val ENTRANCE_SIZE = 24
    }

    private fun Int.toPx() = this * Resources.getSystem().displayMetrics.density.toInt()

    private fun Float.toPx() = this * Resources.getSystem().displayMetrics.density.toFloat()
}