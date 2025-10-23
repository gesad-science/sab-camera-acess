package helpers

import android.graphics.Rect
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import face.FaceDetectorManager
import ui.FaceOverlayView

class ImageAnalysisHelper(
    private val faceDetectorManager: FaceDetectorManager,
    private val faceOverlayView: FaceOverlayView,
    private val previewView: PreviewView,
) {
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    fun createAnalyzer(activity: AppCompatActivity): ImageAnalysis.Analyzer =
        ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage =
                    InputImage
                        .fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees,
                        )

                faceDetectorManager.detectFaces(activity, inputImage) { faces ->
                    if (faces.isNotEmpty()) {
                        instanceFaceDetect(mediaImage, faces, imageProxy.imageInfo.rotationDegrees)
                    }
                    imageProxy.close()
                }
            } else {
                activity.runOnUiThread {
                    faceOverlayView.setFaces(emptyList())
                }
                imageProxy.close()
            }
        }

    private fun instanceFaceDetect(
        mediaImage: Image,
        faces: List<Face>,
        rotationDegrees: Int,
    ) {
        val rects =
            faces.map { face ->
                mapRectToView(
                    face.boundingBox,
                    mediaImage.width,
                    mediaImage.height,
                    rotationDegrees,
                    false,
                )
            }

        faceOverlayView.post {
            faceOverlayView.setFaces(rects)
        }
    }

    private fun mapRectToView(
        rect: Rect,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int,
        isFrontCamera: Boolean,
    ): Rect {
        val viewWidth = previewView.width.toFloat()
        val viewHeight = previewView.height.toFloat()

        val scaleX: Float
        val scaleY: Float
        if (rotationDegrees == ROTATION_DEGREES_MIN || rotationDegrees == ROTATION_DEGREES_MAX) {
            scaleX = viewWidth / imageHeight
            scaleY = viewHeight / imageWidth
        } else {
            scaleX = viewWidth / imageWidth
            scaleY = viewHeight / imageHeight
        }

        var left = rect.left * scaleX
        var top = rect.top * scaleY
        var right = rect.right * scaleX
        var bottom = rect.bottom * scaleY

        if (isFrontCamera) {
            val flippedLeft = viewWidth - right
            val flippedRight = viewWidth - left
            left = flippedLeft
            right = flippedRight
        }
        if (rotationDegrees != ROTATION_DEGREES_MIN && rotationDegrees != ROTATION_DEGREES_MAX) {
            val cx = (left + right) / DIVISOR_SCALE
            val cy = (top + bottom) / DIVISOR_SCALE
            val halfW = (right - left) / DIVISOR_SCALE
            val halfH = (bottom - top) / DIVISOR_SCALE

            val newHalfW = halfH
            val newHalfH = halfW

            left = cx - newHalfW
            right = cx + newHalfW
            top = cy - newHalfH
            bottom = cy + newHalfH
        }
        val cx = (left + right) / DIVISOR_SCALE
        val cy = (top + bottom) / DIVISOR_SCALE
        val halfW = (right - left) / DIVISOR_SCALE
        val halfH = (bottom - top) / DIVISOR_SCALE
        val newHalfW = halfW * WIDTH_FACTOR
        val newHalfH = halfH * HEIGHT_FACTOR
        left = cx - newHalfW
        right = cx + newHalfW
        top = cy - newHalfH
        bottom = cy + newHalfH
        val l = left.coerceIn(MIN_VALUE_CORCE, viewWidth)
        val t = top.coerceIn(MIN_VALUE_CORCE, viewHeight)
        val r = right.coerceIn(MIN_VALUE_CORCE, viewWidth)
        val b = bottom.coerceIn(MIN_VALUE_CORCE, viewHeight)
        return Rect(
            l.toInt(),
            t.toInt(),
            r.toInt(),
            b.toInt(),
        )
    }
}
