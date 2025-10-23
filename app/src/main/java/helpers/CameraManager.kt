package helpers

import android.content.Context
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val cameraExecutor: ExecutorService,
) {
    private var cameraProvider: ProcessCameraProvider? = null
    var imageCapture: ImageCapture? = null
        private set

    @OptIn(ExperimentalGetImage::class)
    fun startCamera(analyzer: ImageAnalysis.Analyzer) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                val cameraProvider = cameraProviderFuture.get()
                this.cameraProvider = cameraProvider

                val resolutionSelector = buildResolutionSelector()
                val preview = buildPreview(resolutionSelector)
                val imageAnalyzer = buildImageAnalyzer(resolutionSelector, analyzer)

                imageCapture =
                    ImageCapture
                        .Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                        .build()

                bindCamera(cameraProvider, preview, imageAnalyzer, imageCapture!!)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun buildResolutionSelector(): ResolutionSelector =
        ResolutionSelector
            .Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(WIDTH_RESOLUTION, HEIGHT_RESOLUTION),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                ),
            ).setAspectRatioStrategy(
                AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY,
            ).build()

    private fun buildPreview(resolutionSelector: ResolutionSelector): Preview =
        Preview
            .Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

    private fun buildImageAnalyzer(
        resolutionSelector: ResolutionSelector,
        analyzer: ImageAnalysis
            .Analyzer,
    ): ImageAnalysis =
        ImageAnalysis
            .Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }

    private fun bindCamera(
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        imageAnalyzer: ImageAnalysis,
        imageCapture: ImageCapture,
    ) {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture,
            )
        } catch (exc: ExceptionInInitializerError) {
            Log.e("CameraInitError", "Error initializing camera", exc)
        }
    }

    fun shutdown() {
        cameraProvider?.unbindAll()
    }
}
