package com.sab.cameraacess.helpers

import android.graphics.Rect
import androidx.camera.view.PreviewView
import face.FaceDetectorManager
import helpers.ImageAnalysisHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ui.viewsmodel.FaceOverlayView

const val MOCK_WIDTH = 1080
const val MOCK_HEIGHT = 1920
const val MOCK_LEFT = 0
const val MOCK_TOP = 0
const val MOCK_RIGHT = 100
const val MOCK_BOTTOM = 200
const val MOCK_IMAGE_WIDTH = 400
const val MOCK_IMAGE_HEIGHT = 800
const val ROTATION_DEGREES = 0
const val IS_FRONT = false

class ImageAnalysisHelperTest {
    private lateinit var helper: ImageAnalysisHelper
    private val faceDetectorManager: FaceDetectorManager = mock()
    private val faceOverlayView: FaceOverlayView = mock()
    private val previewView: PreviewView = mock()

    @Before
    fun setup() {
        whenever(previewView.width).thenReturn(MOCK_WIDTH)
        whenever(previewView.height).thenReturn(MOCK_HEIGHT)
        helper = ImageAnalysisHelper(faceDetectorManager, faceOverlayView, previewView)
        whenever(previewView.width).thenReturn(MOCK_WIDTH)
        whenever(previewView.height).thenReturn(MOCK_HEIGHT)
        helper = ImageAnalysisHelper(faceDetectorManager, faceOverlayView, previewView)
    }

    @Test
    fun `must scale initial values`() {
        val originalRect = Rect(MOCK_LEFT, MOCK_TOP, MOCK_RIGHT, MOCK_BOTTOM)
        val expectedScaleX = MOCK_WIDTH.toFloat() / MOCK_IMAGE_WIDTH.toFloat()
        val expectedScaleY = MOCK_HEIGHT.toFloat() / MOCK_IMAGE_HEIGHT.toFloat()

        val result =
            helper
                .invokePrivateMapRectToView(
                    originalRect,
                    MOCK_IMAGE_WIDTH,
                    MOCK_IMAGE_HEIGHT,
                    ROTATION_DEGREES,
                    IS_FRONT,
                )
        val expectedRight = (originalRect.right * expectedScaleX).toInt()
        val expectedBottom = (originalRect.bottom * expectedScaleY).toInt()
        assertEquals(expectedRight, result.right)
        assertEquals(expectedBottom, result.bottom)
    }

    @Test
    fun `front and back camera must behave the same since flip is not implemented`() {
        val originalRect = Rect(0, 0, 100, 200)

        val noFlip =
            helper
                .invokePrivateMapRectToView(
                    originalRect,
                    MOCK_IMAGE_WIDTH,
                    MOCK_IMAGE_HEIGHT,
                    ROTATION_DEGREES,
                    false,
                )
        val flip =
            helper
                .invokePrivateMapRectToView(
                    originalRect,
                    MOCK_IMAGE_WIDTH,
                    MOCK_IMAGE_HEIGHT,
                    ROTATION_DEGREES,
                    true,
                )
        assertEquals(noFlip.left, flip.left)
        assertEquals(noFlip.right, flip.right)
    }
}

@Suppress("UNCHECKED_CAST")
private fun ImageAnalysisHelper.invokePrivateMapRectToView(
    rect: Rect,
    imageWidth: Int,
    imageHeight: Int,
    rotationDegrees: Int,
    isFrontCamera: Boolean,
): Rect {
    val method =
        ImageAnalysisHelper::class.java
            .getDeclaredMethod(
                "mapRectToView",
                Rect::class.java,
                Int::class.java,
                Int::class.java,
                Int::class.java,
                Boolean::class.java,
            )
    method.isAccessible = true
    return method.invoke(this, rect, imageWidth, imageHeight, rotationDegrees, isFrontCamera) as Rect
}
