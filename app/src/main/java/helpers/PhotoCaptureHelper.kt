package helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import face.FaceDetectorManager
import files.LogHelper
import java.util.concurrent.ExecutorService

class PhotoCaptureHelper(
    private val context: Context,
    private val faceDetectorManager: FaceDetectorManager,
) {
    private val rectPaint =
        Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH_RECT
            isAntiAlias = true
        }

    private val textPaint =
        Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            textSize = TEXT_SIZE
            isAntiAlias = true
        }

    fun takePhoto(
        imageCapture: ImageCapture?,
        executor: ExecutorService,
        onFacesCropped: (List<Bitmap>) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (imageCapture == null) {
            onError("ImageCapture is not initialized")
            return
        }

        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    super.onCaptureSuccess(imageProxy)
                    try {
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val bitmap = imageProxy.toBitmap()?.copy(Bitmap.Config.ARGB_8888, true)
                        if (bitmap == null) {
                            onError("Failed to convert ImageProxy to Bitmap")
                            imageProxy.close()
                            return
                        }

                        cropFaces(bitmap, rotation) { faceBitmaps ->
                            if (faceBitmaps.isNotEmpty()) {
                                onFacesCropped(faceBitmaps)
                            } else {
                                onError("No faces detected")
                            }
                            imageProxy.close()
                        }
                    } catch (e: IllegalArgumentException) {
                        LogHelper.log(context, "Invalid image data: ${e.message}\n${Log.getStackTraceString(e)}")
                        onError("Invalid image data: ${e.message}")
                    } catch (e: OutOfMemoryError) {
                        LogHelper.log(context, "Out of memory while processing image: ${e.message}")
                        onError("Out of memory while processing image")
                    } finally {
                        imageProxy.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    onError("Error taking picture: ${exception.message}")
                }
            },
        )
    }

    private fun cropFaces(
        bitmap: Bitmap,
        rotation: Int,
        onFacesCropped: (List<Bitmap>) -> Unit,
    ) {
        val inputImage = InputImage.fromBitmap(bitmap, rotation)
        faceDetectorManager.detectFaces(context, inputImage) { faces ->
            val faceBitmaps =
                faces.mapNotNull { face ->
                    try {
                        val rect =
                            mapFaceRectToBitmap(
                                face.boundingBox,
                                bitmap.width,
                                bitmap.height,
                            )
                        val canvas = Canvas(bitmap)
                        canvas.drawRect(rect, rectPaint)
                        canvas.drawText(
                            "Face",
                            rect.left.toFloat(),
                            (rect.top - DRAWN_TEXT_TOP).coerceAtLeast(MINIMUM_VALUE_DRAWN).toFloat(),
                            textPaint,
                        )
                        Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
                    } catch (e: IllegalArgumentException) {
                        LogHelper.log(context, "Invalid crop rect: ${e.message}")
                        null
                    } catch (e: OutOfMemoryError) {
                        LogHelper.log(context, "Out of memory while cropping face : ${e.stackTrace}")
                        null
                    }
                }
            onFacesCropped(faceBitmaps)
        }
    }

    private fun mapFaceRectToBitmap(
        faceRect: Rect,
        imageWidth: Int,
        imageHeight: Int,
    ): Rect {
        val left = faceRect.left.coerceIn(0, imageWidth)
        val top = faceRect.top.coerceIn(0, imageHeight)
        val right = faceRect.right.coerceIn(0, imageWidth)
        val bottom = faceRect.bottom.coerceIn(0, imageHeight)
        return Rect(left, top, right, bottom)
    }

    companion object {
        private const val STROKE_WIDTH_RECT = 4f
        private const val TEXT_SIZE = 36f
        private const val DRAWN_TEXT_TOP = 20
        private const val MINIMUM_VALUE_DRAWN = 0
    }
}
