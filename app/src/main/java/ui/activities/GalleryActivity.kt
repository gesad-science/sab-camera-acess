package ui.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.sab.cameraacess.R
import java.io.File

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val imageView = findViewById<ImageView>(R.id.imageViewFull)

        val photoPath = intent.getStringExtra("photo_path")

        if (photoPath != null) {
            val photoFile = File(photoPath)
            if (photoFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        } else {
            val photosDir = getExternalFilesDir("photos") ?: filesDir
            val allPhotos = photosDir.listFiles()?.sortedByDescending { it.lastModified() }

            if (!allPhotos.isNullOrEmpty()) {
                val lastPhoto = allPhotos.first()
                val bitmap = BitmapFactory.decodeFile(lastPhoto.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }
    }
}