package ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
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
import ui.viewsmodel.FaceOverlayView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var faceDetectorManager: FaceDetectorManager
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceOverlayView: FaceOverlayView
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private lateinit var cameraManager: CameraManager
    private lateinit var imageAnalysisHelper: ImageAnalysisHelper
    private lateinit var photoCaptureHelper: PhotoCaptureHelper
    private lateinit var apiUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        apiUrl = intent.getStringExtra("api_url") ?: ""
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

        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            takePhoto()
        }
        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
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
                FaceApiHelper.sendFacesToApi(this, faceBitmaps, apiUrl)
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