package ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sab.cameraacess.R
import face.FaceDetectorManager
import photos.PhotoManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var photoManager: PhotoManager
    private lateinit var faceDetectorManager: FaceDetectorManager
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        photoManager = PhotoManager(this)
        faceDetectorManager = FaceDetectorManager()
        cameraExecutor = Executors.newSingleThreadExecutor()
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        findViewById<ImageButton>(R.id.buttonCameraIcon).setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
