package ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sab.cameraacess.R
import face.FaceDetectorManager
import files.LogHelper
import helpers.CameraManager
import helpers.FaceApiHelper
import helpers.ImageAnalysisHelper
import helpers.MARGIN_TOP_POPUP
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
    private lateinit var function: String
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        apiUrl = intent.getStringExtra("api_url") ?: ""
        function = intent.getStringExtra("function") ?: ""
        userName = intent.getStringExtra("username") ?: "User"
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
            showSnackbar("Photo sent for recognition")
        }
        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("function", function)
            intent.putExtra("username", userName)
            startActivity(intent)
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

    fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, duration)
        val view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = MARGIN_TOP_POPUP
        view.layoutParams = params
        snackbar.show()
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
