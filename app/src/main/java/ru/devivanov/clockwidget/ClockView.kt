package ru.devivanov.clockwidget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*

class ClockView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    private lateinit var bitmap: Bitmap
    private var isStaticPictureDrawn: Boolean = false
    private lateinit var staticCanvas: Canvas

    private lateinit var paintClockCircle: Paint
    private lateinit var dashPaintThin: Paint
    private lateinit var clockPaint: Paint

    private var radius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var scaleSize = 60f

    private var dashColor = Color.WHITE
    private var digitColor = Color.WHITE
    private var arrowColor = Color.RED

    private val rect = Rect()
    private val numerals = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        val minSide = Math.min(chosenWidth, chosenHeight)
        centerX = minSide.div(2f)
        centerY = minSide.div(2f)

        setMeasuredDimension(minSide, minSide)
    }

    init {
        initDrawingTools()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = if (width > height) {
            height.div(2f)
        } else {
            width.div(2f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isStaticPictureDrawn) {
            drawStaticPicture()
        }
        canvas.drawBitmap(bitmap, centerX - radius, centerY - radius, null)
        drawHands(canvas)

        postInvalidateDelayed(500)
    }

    private fun chooseDimension(mode: Int, size: Int) =
        when (mode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> size
            else -> 300
        }

    private fun initDrawingTools() {
        dashPaintThin = Paint().apply {
            color = dashColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 0.01f
            isAntiAlias = true
        }
        clockPaint = Paint(dashPaintThin).apply {
            strokeWidth = 2f
            textSize = scaleSize * 1.5f
            color = digitColor
            isAntiAlias = true
        }
        paintClockCircle = Paint().apply {
            color = Color.BLACK
            strokeWidth = 10f
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    private fun drawStaticPicture() {
        bitmap = Bitmap.createBitmap(
            (centerX * 2).toInt(),
            (centerY * 2).toInt(),
            Bitmap.Config.ARGB_8888
        )
        staticCanvas = Canvas(bitmap)
        drawClock(staticCanvas)

        isStaticPictureDrawn = true
    }

    private fun drawClock(canvas: Canvas) {
        canvas.save()

        canvas.translate(centerX, centerY)

        canvas.drawCircle(0f, 0f, radius, paintClockCircle)

        for (number in numerals) {
            val text = number.toString()
            val digitOffset = 0.9f

            clockPaint.getTextBounds(text, 0, text.length, rect)

            val angle = Math.PI / 6 * (number - 3)

            val x = (Math.cos(angle) * radius * digitOffset - rect.width() / 2).toFloat()
            val y = (Math.sin(angle) * radius * digitOffset + rect.height() / 2).toFloat()

            canvas.drawText(text, x, y, clockPaint)
        }
    }
    private fun drawHands(canvas: Canvas) {
        canvas.save()
        canvas.translate(centerX, centerY)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)

        drawHand(
            canvas,
            ((hour + calendar.get(Calendar.MINUTE) / 60.0) * 5f),
            HOUR_HAND
        )
        drawHand(canvas, calendar.get(Calendar.MINUTE).toDouble(), MINUTE_HAND)
        drawHand(canvas, calendar.get(Calendar.SECOND).toDouble(), SECOND_HAND)

        canvas.restore()
    }

    private fun drawHand(canvas: Canvas, loc: Double, hand: Int) {
        val paintHands = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            isAntiAlias = true


            when (hand) {
                HOUR_HAND -> strokeWidth = scaleSize * 0.5f
                MINUTE_HAND -> strokeWidth = scaleSize * 0.3f
                SECOND_HAND -> {
                    strokeWidth = scaleSize * 0.2f
                    color = arrowColor
                }
            }
        }
        val angle = Math.PI * loc / 30 - Math.PI / 2
        val handRadius = if (hand == HOUR_HAND) {
            radius * 0.7
        } else {
            radius * 0.9
        }
        canvas.drawLine(
            0f, 0f,
            (Math.cos(angle) * handRadius).toFloat(),
            (Math.sin(angle) * handRadius).toFloat(),
            paintHands
        )
    }

    companion object {
        const val HOUR_HAND = 1
        const val MINUTE_HAND = 2
        const val SECOND_HAND = 3
    }
}