package com.trunglt.democamerax

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.core.content.ContextCompat


class DrawingView(context: Context, attrs: AttributeSet) : QrScannerView(context, attrs) {
    private val corners = mutableListOf<Point>()
    private val faceRectList = mutableListOf<Rect>()
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
        super.onDraw(canvas)
        drawFaces(canvas)
    }

    fun setFaceRectList(rectList: List<Rect>) {
        faceRectList.clear()
        faceRectList.addAll(rectList)
        postInvalidate()
    }

    fun setCorners(corners: List<Point>) {
        this.corners.clear()
        this.corners.addAll(corners)
        postInvalidate()
    }

    private fun drawFaces(canvas: Canvas) {
        try {
//            faceRectList.forEach {
//                canvas.save()
//                canvas.skew(0.15f, 0.0f)
//                canvas.drawLine(it.left.toFloat(), it.top.div(1.15).toFloat(), it.right.toFloat(), it.top.toFloat(), rectBorderPaint)
//                canvas.drawRect(it, rectBorderPaint)
//                canvas.restore()
//            }
            canvas.drawLine(
                corners[0].x.toFloat(),
                corners[0].y.toFloat(), corners[1].x.toFloat(),
                corners[1].y.toFloat(), rectBorderPaint
            )
            canvas.drawLine(
                corners[1].x.toFloat(),
                corners[1].y.toFloat(), corners[2].x.toFloat(),
                corners[2].y.toFloat(), rectBorderPaint
            )
            canvas.drawLine(
                corners[2].x.toFloat(),
                corners[2].y.toFloat(), corners[3].x.toFloat(),
                corners[3].y.toFloat(), rectBorderPaint
            )
            canvas.drawLine(
                corners[3].x.toFloat(),
                corners[3].y.toFloat(), corners[0].x.toFloat(),
                corners[0].y.toFloat(), rectBorderPaint
            )
        } catch (e: Exception) {

        }
    }
}