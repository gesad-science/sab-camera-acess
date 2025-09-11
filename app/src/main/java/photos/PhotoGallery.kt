package photos

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sab.cameraacess.R
import java.io.File

class PhotoGallery(
    private val photos: MutableList<File>,
) : RecyclerView.Adapter<PhotoGallery.PhotoViewHolder>() {
    companion object {
        private const val SPAN_COUNT = 3
        private const val SPACING = 4
    }

    class PhotoViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView =
            view.findViewById(
                R.id.imageViewItem,
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PhotoViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.gallery_photo, parent, false)

        val totalSpacing = SPACING * (SPAN_COUNT - 1)
        val size = (parent.measuredWidth - totalSpacing) / SPAN_COUNT
        view.layoutParams.height = size

        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PhotoViewHolder,
        position: Int,
    ) {
        val bitmap = BitmapFactory.decodeFile(photos[position].absolutePath)
        holder.imageView.setImageBitmap(bitmap)
    }

    override fun getItemCount(): Int = photos.size

    fun updatePhotos(newPhotos: List<File>) {
        photos.clear()
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }
}
