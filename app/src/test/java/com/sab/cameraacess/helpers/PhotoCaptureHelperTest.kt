package com.sab.cameraacess.helpers

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoCaptureHelperTest {
    private val helper = PhotoCaptureHelperTestWrapper()

    @Test
    fun `must clamp values below zero`() {
        val rect = Rect(-50, -30, 200, 300)

        val result = helper.mapFaceRectToBitmap(rect, 1000, 1200)

        assertEquals(0, result.left)
        assertEquals(0, result.top)
    }
}

class PhotoCaptureHelperTestWrapper {
    fun mapFaceRectToBitmap(
        rect: Rect,
        width: Int,
        height: Int,
    ): Rect {
        val left = rect.left.coerceIn(0, width)
        val top = rect.top.coerceIn(0, height)
        val right = rect.right.coerceIn(0, width)
        val bottom = rect.bottom.coerceIn(0, height)
        return Rect(left, top, right, bottom)
    }
}
