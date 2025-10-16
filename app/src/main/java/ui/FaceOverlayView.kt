package ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

const val STROKE_WIDTH = 5f
const val CORNER_LENGTH = 30f

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
            drawFaceCorners(canvas, face)
        }
    }

    private fun drawFaceCorners(
        canvas: Canvas,
        face: Rect,
    ) {
        val corners =
            listOf(
                Pair(face.left.toFloat(), face.top.toFloat()),
                Pair(face.right.toFloat(), face.top.toFloat()),
                Pair(face.left.toFloat(), face.bottom.toFloat()),
                Pair(face.right.toFloat(), face.bottom.toFloat()),
            )

        corners.forEach { (x, y) ->
            drawCorner(canvas, x, y, face)
        }
    }

    private fun drawCorner(
        canvas: Canvas,
        x: Float,
        y: Float,
        face: Rect,
    ) {
        val horizontal =
            if (x > (face.left + face.right) / 2) {
                -CORNER_LENGTH
            } else {
                CORNER_LENGTH
            }
        val vertical =
            if (y > (face.top + face.bottom) / 2) {
                -CORNER_LENGTH
            } else {
                CORNER_LENGTH
            }
        canvas.drawLine(
            x,
            y,
            x + horizontal,
            y,
            paint,
        )

        canvas.drawLine(
            x,
            y,
            x,
            y + vertical,
            paint,
        )
    }
}
