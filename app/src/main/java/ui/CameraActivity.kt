package ui

import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

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
            onPhotoSaved = { photoPath ->
                LogHelper.log(this, "Image saved: $photoPath")
                val intent = Intent(this, GalleryActivity::class.java)
                intent.putExtra("photo_path", photoPath)
                startActivity(intent)
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
