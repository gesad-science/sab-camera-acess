package ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sab.cameraacess.R
import face.FaceDetectorManager
import photos.PhotoManager

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var photoManager: PhotoManager
    private lateinit var faceDetectorManager: FaceDetectorManager

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    imageView.setImageBitmap(it)
                    photoManager.saveBitmap(it)
                    faceDetectorManager.detectFaces(it)
                }
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openCamera()
        }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePicture.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageViewPhoto)
        photoManager = PhotoManager(this)
        faceDetectorManager = FaceDetectorManager()
        val buttonCamera = findViewById<Button>(R.id.buttonCamera)
        val buttonGallery = findViewById<Button>(R.id.buttonGallery)
        buttonCamera.setOnClickListener {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }
        buttonGallery.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }
}
