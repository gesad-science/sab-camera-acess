package ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sab.cameraacess.R
import photos.PhotoGallery
import photos.PhotoManager

class GalleryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var photoGallery: PhotoGallery

    companion object {
        private const val SPAN_COUNT = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        recyclerView = findViewById(R.id.recyclerViewGallery)
        recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT)

        val photoManager = PhotoManager(this)
        val photos = photoManager.getAllPhotos().toMutableList()
        photoGallery = PhotoGallery(photos)
        recyclerView.adapter = photoGallery
    }

    override fun onResume() {
        super.onResume()
        val photoManager = PhotoManager(this)
        val photos = photoManager.getAllPhotos()
        photoGallery.updatePhotos(photos)
    }
}
