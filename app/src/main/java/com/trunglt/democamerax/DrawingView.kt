package com.trunglt.democamerax

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat


class DrawingView(context: Context, attrs: AttributeSet) : QrScannerView(context, attrs) {
    private var isAnimationRunning = false
    private val mCorners = mutableListOf(
        AnimatablePoint(),
        AnimatablePoint(),
        AnimatablePoint(),
        AnimatablePoint()
    )
    private val rectBorderPaint by lazy {
        Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
            color = ContextCompat.getColor(context, R.color.white)
            strokeWidth = 4f
        }
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas) {
        drawBoundingBox(canvas)
    }

    fun setCorners(corners: List<AnimatablePoint>) {
        if (isAnimationRunning) {
            return
        }
        val animateLeft: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[0],
            "x",
            mCorners[0].x.toFloat(),
            corners[0].x.toFloat()
        )
        val animateLeft2: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[0],
            "y",
            mCorners[0].y.toFloat(),
            corners[0].y.toFloat()
        )
        val animateRight: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[1],
            "x",
            mCorners[1].x.toFloat(),
            corners[1].x.toFloat()
        )
        val animateRight2: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[1],
            "y",
            mCorners[1].y.toFloat(),
            corners[1].y.toFloat()
        )
        val animateTop: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[2],
            "x",
            mCorners[2].x.toFloat(),
            corners[2].x.toFloat()
        )
        val animateTop2: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[2],
            "y",
            mCorners[2].y.toFloat(),
            corners[2].y.toFloat()
        )
        val animateBottom: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[3],
            "x",
            mCorners[3].x.toFloat(),
            corners[3].x.toFloat()
        )
        val animateBottom2: ObjectAnimator = ObjectAnimator.ofFloat(
            mCorners[3],
            "y",
            mCorners[3].y.toFloat(),
            corners[3].y.toFloat()
        )
        animateBottom.addUpdateListener { postInvalidate() }
        animateBottom.doOnStart {
            isAnimationRunning = true
        }
        animateBottom.doOnEnd {
            isAnimationRunning = false
            mCorners.clear()
            mCorners.addAll(corners)
        }
        val rectAnimation = AnimatorSet()
        rectAnimation.playTogether(
            animateLeft,
            animateRight,
            animateTop,
            animateBottom,
            animateLeft2,
            animateRight2,
            animateTop2,
            animateBottom2
        )
        rectAnimation.setDuration(250).start()
    }

    fun getDefaultBoxCorners(): List<AnimatablePoint> {
        return listOf(
            AnimatablePoint(mLeft.toInt(), mTop.toInt()),
            AnimatablePoint(mRight.toInt(), mTop.toInt()),
            AnimatablePoint(mRight.toInt(), mBottom.toInt()),
            AnimatablePoint(mLeft.toInt(), mBottom.toInt())
        )
    }

    private fun drawBoundingBox(canvas: Canvas) {
        try {
            canvas.drawLine(
                mCorners[0].x.toFloat(),
                mCorners[0].y.toFloat(), mCorners[1].x.toFloat(),
                mCorners[1].y.toFloat(), rectBorderPaint
            )
            canvas.drawLine(
                mCorners[1].x.toFloat(),
                mCorners[1].y.toFloat(), mCorners[2].x.toFloat(),
                mCorners[2].y.toFloat(), rectBorderPaint
            )
            canvas.drawLine(
                mCorners[2].x.toFloat(),
                mCorners[2].y.toFloat(), mCorners[3].x.toFloat(),
                mCorners[3].y.toFloat(), rectBorderPaint
            )
            canvas.drawLine(
                mCorners[3].x.toFloat(),
                mCorners[3].y.toFloat(), mCorners[0].x.toFloat(),
                mCorners[0].y.toFloat(), rectBorderPaint
            )
        } catch (e: Exception) {

        }
    }

    class AnimatablePoint : Point {
        constructor() : super() {}
        constructor(x: Int, y: Int) : super() {
            this.x = x
            this.y = y
        }

        var x: Float
            get() = super.x.toFloat()
            set(value) {
                this.x = value.toInt()
            }
        var y: Float
            get() = super.y.toFloat()
            set(value) {
                this.y = value.toInt()
            }
    }
}