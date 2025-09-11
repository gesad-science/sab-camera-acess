package photos

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

class PhotoManager(
    private val context: Context,
) {
    companion object {
        private const val JPEG_QUALITY = 100
    }

    fun saveBitmap(bitmap: Bitmap): String {
        val directory = context.getExternalFilesDir("photos")
        if (directory?.exists() == false) {
            directory.mkdirs()
        }
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }

        return file.absolutePath
    }

    fun getAllPhotos(): List<File> {
        val directory = context.getExternalFilesDir("photos")
        return directory?.listFiles()?.toList() ?: emptyList()
    }
}
