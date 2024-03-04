package com.trunglt.democamerax

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceView
import androidx.core.content.ContextCompat


class DrawingView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs) {
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

    private fun drawFaces(canvas: Canvas) {
        try {
            faceRectList.forEach {
                canvas.drawRect(it, rectBorderPaint)
            }
        } catch (e: Exception) {

        }
    }
}