package helpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import face.FaceDetectorManager
import java.io.File
import java.io.FileOutputStream
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
        onPhotoSaved: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (imageCapture == null) {
            onError("ImageCapture is not initialized")
            return
        }

        val photosDir = context.getExternalFilesDir("photos") ?: context.filesDir
        val photoFile =
            File(
                photosDir,
                "photo_${System.currentTimeMillis()}.jpg",
            )

        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    super.onCaptureSuccess(imageProxy)
                    try {
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val bitmap = imageProxy.toBitmap()?.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                        if (bitmap == null) {
                            onError("Failed to convert ImageProxy to Bitmap")
                            imageProxy.close()
                            return
                        }

                        val inputImage = InputImage.fromBitmap(bitmap, rotation)
                        faceDetectorManager.detectFaces(context, inputImage) { faces ->
                            if (faces.isNotEmpty()) {
                                val canvas = Canvas(bitmap)
                                faces.forEach { face ->
                                    val rect =
                                        mapFaceRectToBitmap(
                                            face.boundingBox,
                                            bitmap.width,
                                            bitmap.height,
                                        )
                                    canvas.drawRect(rect, rectPaint)
                                    canvas.drawText(
                                        "Face",
                                        rect.left.toFloat(),
                                        (rect.top - DRAWN_TEXT_TOP).coerceAtLeast(MINIMUM_VALUE_DRAWN).toFloat(),
                                        textPaint,
                                    )
                                }
                            }
                            FileOutputStream(photoFile).use { out ->
                                if (bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, QUALITY_IMAGE, out)) {
                                    onPhotoSaved(photoFile.absolutePath)
                                } else {
                                    onError("Failed to save image")
                                }
                            }
                            imageProxy.close()
                        }
                    } catch (e: IllegalArgumentException) {
                        onError("Illegal argument: ${e.message}")
                        imageProxy.close()
                    } catch (e: IllegalStateException) {
                        onError("Illegal state: ${e.message}")
                        imageProxy.close()
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

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    onError("Error in take picture: ${exception.message}")
                }
            },
        )
    }
}
