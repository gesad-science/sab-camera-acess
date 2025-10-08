package ui

import android.media.Image
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.sab.cameraacess.R
import face.FaceDetectorManager
import files.LogHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val MIN_VALUE_CORCE = 0f
const val ROTATION_DEGREES_MIN = 90
const val ROTATION_DEGREES_MAX = 270
const val WIDTH_FACTOR = 1f
const val HEIGHT_FACTOR = 1.3f
const val DIVISOR_SCALE = 2f

class CameraActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var faceDetectorManager: FaceDetectorManager
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceOverlayView: FaceOverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        previewView = findViewById(R.id.previewView)
        faceDetectorManager = FaceDetectorManager()
        faceOverlayView = findViewById(R.id.faceOverlay)
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview =
                Preview
                    .Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

            val imageAnalyzer =
                ImageAnalysis
                    .Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val inputImage =
                                    InputImage
                                        .fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees,
                                        )

                                faceDetectorManager.detectFaces(this, inputImage) { faces ->
                                    if (faces.isNotEmpty()) {
                                        instanceFaceDetect(mediaImage, faces, imageProxy.imageInfo.rotationDegrees)
                                    }
                                    imageProxy.close()
                                }
                            } else {
                                runOnUiThread {
                                    faceOverlayView.setFaces(emptyList())
                                }
                                imageProxy.close()
                            }
                        }
                    }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer,
                )
            } catch (exc: IllegalArgumentException) {
                exc.printStackTrace()
            } catch (exc: IllegalStateException) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun instanceFaceDetect(
        mediaImage: Image,
        faces: List<Face>,
        rotationDegrees: Int,
    ) {
        val imageWidth = mediaImage.width
        val imageHeight = mediaImage.height
        val rects =
            faces.map { face ->
                mapRectToView(face.boundingBox, imageWidth, imageHeight, rotationDegrees, false)
            }

        runOnUiThread {
            faceOverlayView.setFaces(rects)
        }

        for (face in faces) {
            LogHelper.log(this, "Face detected: ${face.boundingBox}")
        }
    }

    private fun mapRectToView(
        rect: android.graphics.Rect,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int,
        isFrontCamera: Boolean,
    ): android.graphics.Rect {
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
        return android.graphics.Rect(
            l.toInt(),
            t.toInt(),
            r.toInt(),
            b.toInt(),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
