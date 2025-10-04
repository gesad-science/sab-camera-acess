package ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

const val STROKE_WIDTH = 5f

class FaceOverlayView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint =
        Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH
        }
    private var faces: List<Rect> = emptyList()

    fun setFaces(faces: List<Rect>) {
        this.faces = faces
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (face in faces) {
            canvas.drawRect(face, paint)
        }
    }
}
