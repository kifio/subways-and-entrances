package kifio

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory

class BitmapMaker {

    private val bitmaps = mutableMapOf<String, Bitmap>()
    private val matrix = Matrix()

    fun getSubwayIcon(ctx: Context, key: String, color: String, size: Int): Bitmap {
        var bitmap = bitmaps[key]
        if (bitmap == null) {
            bitmap = buildStationLogo(ctx, color, size.toPx())
        }
        return bitmap
    }

    private fun buildStationLogo(ctx: Context, color: String, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawBackground(color, size, canvas)
        drawRoundedBitmapDrawable(ctx.resources, bitmap, canvas)
        drawLogo(ctx, size, canvas)
        return bitmap
    }

    private fun drawBackground(color: String, size: Int, canvas: Canvas) {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(Color.parseColor(color))
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setStroke(STROKE_WIDTH.toPx(), Color.WHITE)
        gradientDrawable.setBounds(0, 0, size, size)
        gradientDrawable.draw(canvas)
    }

    private fun drawRoundedBitmapDrawable(resources: Resources, bitmap: Bitmap, canvas: Canvas) {
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedBitmapDrawable.setAntiAlias(true)
        roundedBitmapDrawable.cornerRadius = SUBWAY_ICON_CORNER
        roundedBitmapDrawable.draw(canvas)
    }

    private fun drawLogo(ctx: Context, size: Int, canvas: Canvas) {
        val drawable = ContextCompat.getDrawable(ctx, R.drawable.metro_white)
        canvas.drawBitmap(fromDrawable(checkNotNull(drawable), size), matrix, null)
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
    }

    fun Int.toPx() = this * Resources.getSystem().displayMetrics.density.toInt()
}