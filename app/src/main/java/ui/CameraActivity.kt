package ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.sab.cameraacess.R
import face.FaceDetectorManager
import files.LogHelper
import helpers.CameraManager
import helpers.FaceApiHelper
import helpers.ImageAnalysisHelper
import helpers.PhotoCaptureHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var faceDetectorManager: FaceDetectorManager
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceOverlayView: FaceOverlayView
    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    private lateinit var cameraManager: CameraManager
    private lateinit var imageAnalysisHelper: ImageAnalysisHelper
    private lateinit var photoCaptureHelper: PhotoCaptureHelper
    private lateinit var apiUrl: String
    private lateinit var modelName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        apiUrl = intent.getStringExtra("api_url") ?: ""
        modelName = intent.getStringExtra("model_name") ?: ""
        initViews()
        initDependencies()
        setupPermissions()
        setupClickListeners()
    }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        faceOverlayView = findViewById(R.id.faceOverlay)
    }

    private fun initDependencies() {
        faceDetectorManager = FaceDetectorManager()
        cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysisHelper = ImageAnalysisHelper(faceDetectorManager, faceOverlayView, previewView)
        photoCaptureHelper = PhotoCaptureHelper(this, faceDetectorManager)
        cameraManager = CameraManager(this, this, previewView, cameraExecutor)
    }

    private fun setupPermissions() {
        permissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                if (granted) {
                    startCamera()
                } else {
                    LogHelper.log(this, "Camera permission denied")
                    finish()
                }
            }

        val permission = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun setupClickListeners() {
        findViewById<android.widget.Button>(R.id.btnTakePhoto).setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val analyzer = imageAnalysisHelper.createAnalyzer(this)
        cameraManager.startCamera(analyzer)
    }

    private fun takePhoto() {
        photoCaptureHelper.takePhoto(
            cameraManager.imageCapture,
            cameraExecutor,
            onFacesCropped = { faceBitmaps ->
                if (faceBitmaps.isEmpty()) {
                    LogHelper.log(this, "No faces detected")
                    return@takePhoto
                }
                val finalUrl = "$apiUrl/v1/$modelName"
                FaceApiHelper.sendFacesToApi(this, faceBitmaps, finalUrl)
            },
            onError = { error ->
                LogHelper.log(this, error)
            },
        )
    }

    override fun onStart() {
        super.onStart()
        if (!::cameraExecutor.isInitialized || cameraExecutor.isShutdown) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
        cameraManager.shutdown()
    }
}
