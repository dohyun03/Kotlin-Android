package com.androidbook.bookmarket.mypage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    public val paint = Paint()
    private val pathList = mutableListOf<CustomPath>()
    public var path = CustomPath(paint.color, paint.strokeWidth)
    init{
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 1.0f

    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                pathList.add(path)
                path.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                path = CustomPath(path.color, path.width)
            }
        }

        return true
    }


    fun setPathColor(color: Int){
        path = CustomPath(color, path.width)
    }


    fun setPathWidth(width: Int){
             path = CustomPath(path.color, width.toFloat())
    }


    fun undoPath(){
        if(pathList.isNotEmpty()){
            pathList.removeAt(pathList.lastIndex)
            invalidate()
        }
    }

    fun clearCanvas(){

        pathList.clear()
        invalidate()


    }

    override fun onDraw(canvas: Canvas) {
        for(path in pathList){
            paint.color = path.color
            paint.strokeWidth = path.width
            canvas.drawPath(path, paint)
        }
    }

    public data class CustomPath(val color: Int, val width: Float) : Path()
}