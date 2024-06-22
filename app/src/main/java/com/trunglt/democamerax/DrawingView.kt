package com.trunglt.democamerax

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toRect
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.math.atan2


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        private const val DEFAULT_QR_SIZE = 800
        private const val DEFAULT_QR_BORDER_WIDTH = 20
        private const val DEFAULT_QR_BORDER_RADIUS = 40
        private const val DEFAULT_QR_IMAGE_PADDING = 160
    }

    var barcode: Barcode? = null
        set(value) {
            field = value
            srcDegree = calculateAngle(
                value?.cornerPoints!![0],
                value.cornerPoints!![1],
                value.cornerPoints!![2],
                value.cornerPoints!![3],
            )
        }
    private var srcDegree = 0
    private var degree = 0
    private var isZoomOut = false
    private var isAnimating = false
    private var qrSize: Int = DEFAULT_QR_SIZE
    private var qrBorderWidth: Int = DEFAULT_QR_BORDER_WIDTH
    private var qrBorderRadius: Int = DEFAULT_QR_BORDER_RADIUS
        set(value) {
            field = value
            qrBorderRadiusAnimatedValue = value
        }
    private var qrBorderRadiusAnimatedValue = DEFAULT_QR_BORDER_RADIUS
    private var qrImagePadding: Int = DEFAULT_QR_IMAGE_PADDING
    private var qrBorderColor: Int = Color.WHITE
    private val qrBitmap by lazy {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            barcode?.rawValue.orEmpty(),
            BarcodeFormat.QR_CODE,
            qrSize,
            qrSize
        )

        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        bitmap
    }
    private val srcRect by lazy {
        AnimatableRectF(
            cx - qrSize / 2,
            cy - qrSize / 2,
            cx + qrSize / 2,
            cy + qrSize / 2
        )
    }
    private val qrRect by lazy {
        AnimatableRectF(
            cx - qrSize / 2,
            cy - qrSize / 2,
            cx + qrSize / 2,
            cy + qrSize / 2
        )
    }
    private val qrImageRect by lazy {
        qrRect.toRect()
    }
    private val cx
        get() = measuredWidth / 2f
    private val cy
        get() = measuredHeight / 3f
    private val qrBorderPaint by lazy {
        Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
        }
    }
    private val qrImagePaint by lazy {
        Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
            color = Color.WHITE
        }
    }

    private val zoomOut = {
        isZoomOut = true
        startZoomAnimation(srcRect.toRect())
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawingView)
        qrSize =
            typedArray.getDimensionPixelOffset(R.styleable.DrawingView_qr_size, DEFAULT_QR_SIZE)
        qrBorderWidth = typedArray.getDimensionPixelOffset(
            R.styleable.DrawingView_qr_border_width,
            DEFAULT_QR_BORDER_WIDTH
        )
        qrBorderRadius = typedArray.getDimensionPixelOffset(
            R.styleable.DrawingView_qr_border_radius,
            DEFAULT_QR_BORDER_RADIUS
        )
        qrImagePadding = typedArray.getDimensionPixelOffset(
            R.styleable.DrawingView_qr_image_padding,
            DEFAULT_QR_IMAGE_PADDING
        )
        qrBorderColor = typedArray.getColor(R.styleable.DrawingView_qr_border_color, Color.WHITE)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawQrRect(canvas)
    }

    fun animateToDetectedRectF(rect: Rect) {
        if (isAnimating) return
        startZoomAnimation(rect) {
            zoomOut.invoke()
        }
    }

    private fun startZoomAnimation(
        rect: Rect,
        onComplete: (() -> Unit)? = null
    ) {
        val rectAnimation = AnimatorSet()
        val animateLeft: ObjectAnimator = ObjectAnimator.ofFloat(
            qrRect,
            "left",
            qrRect.left,
            rect.left.toFloat()
        ).apply {
            addUpdateListener {
                qrRect.left = it.animatedValue as Float
            }
        }
        val animateRight: ObjectAnimator = ObjectAnimator.ofFloat(
            qrRect,
            "right",
            qrRect.right,
            rect.right.toFloat()
        ).apply {
            addUpdateListener {
                qrRect.right = it.animatedValue as Float
            }
        }
        val animateTop: ObjectAnimator = ObjectAnimator.ofFloat(
            qrRect,
            "top",
            qrRect.top,
            rect.top.toFloat()
        ).apply {
            addUpdateListener {
                qrRect.top = it.animatedValue as Float
            }
        }
        val animateBottom: ObjectAnimator = ObjectAnimator.ofFloat(
            qrRect,
            "bottom",
            qrRect.bottom,
            rect.bottom.toFloat()
        ).apply {
            addUpdateListener {
                qrRect.bottom = it.animatedValue as Float
                postInvalidate()
            }
        }
        rectAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isAnimating = true
            }

            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
                onComplete?.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {
                isAnimating = false
            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        val degreeAnimation = if (isZoomOut) {
            ValueAnimator.ofInt(srcDegree, 0)
        } else {
            ValueAnimator.ofInt(0, srcDegree)
        }.apply {
            addUpdateListener {
                degree = it.animatedValue as Int
            }
        }
//        val qrBorderRadiusAnimation = if (isZoomOut) {
//            ValueAnimator.ofInt(0, qrBorderRadius)
//        } else {
//            ValueAnimator.ofInt(qrBorderRadius, 0)
//        }.apply {
//            addUpdateListener {
//                qrBorderRadiusAnimatedValue = it.animatedValue as Int
//            }
//        }
        rectAnimation.playTogether(
            animateLeft,
            animateRight,
            animateTop,
            animateBottom,
            degreeAnimation,
//            qrBorderRadiusAnimation
        )
        rectAnimation.setDuration(250).start()
    }

    private fun drawQrRect(canvas: Canvas) {
        canvas.save()
        canvas.rotate(degree.toFloat(), qrRect.centerX(), qrRect.centerY())
        canvas.drawRoundRect(
            qrRect,
            qrBorderRadiusAnimatedValue.toFloat(),
            qrBorderRadiusAnimatedValue.toFloat(),
            qrBorderPaint.apply {
                color = qrBorderColor
                strokeWidth = qrBorderWidth.toFloat()
                if (isZoomOut) {
                    style = Paint.Style.FILL_AND_STROKE
                }
            }
        )
        if (isZoomOut) {
            qrImageRect.apply {
                left = (qrRect.left + qrImagePadding).toInt()
                top = (qrRect.top + qrImagePadding).toInt()
                right = (qrRect.right - qrImagePadding).toInt()
                bottom = (qrRect.bottom - qrImagePadding).toInt()
            }
            canvas.drawBitmap(qrBitmap, null, qrImageRect, null)
        }
        canvas.restore()
    }

    private fun calculateAngle(topLeft: Point, topRight: Point, bottomRight: Point, bottomLeft: Point): Int {
        val deltaX = (topRight.x - topLeft.x).toFloat()
        val deltaY = (topRight.y - topLeft.y).toFloat()
        val angle = atan2(deltaY, deltaX)
        return Math.toDegrees(angle.toDouble()).toInt().also {
            println(it)
        }
    }
}