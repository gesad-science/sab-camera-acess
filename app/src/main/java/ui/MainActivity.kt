package ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sab.cameraacess.R
import face.FaceDetectorManager
import photos.PhotoManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var photoManager: PhotoManager
    private lateinit var faceDetectorManager: FaceDetectorManager
    private lateinit var cameraExecutor: ExecutorService
    // private var imageCapture: ImageCapture? = null

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            } else {
                Log.e("CameraX", "Permissão negada")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        photoManager = PhotoManager(this)
        faceDetectorManager = FaceDetectorManager()
        cameraExecutor = Executors.newSingleThreadExecutor()

        findViewById<Button>(R.id.buttonGallery).setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        findViewById<Button>(R.id.buttonCamera).setOnClickListener {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
