package photos

import android.content.Context
import java.io.File

class PhotoManager(
    private val context: Context,
) {
    fun createPhotoFile(): File {
        val directory = context.getExternalFilesDir("photos")
        if (directory?.exists() == false) {
            directory.mkdirs()
        }
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        return File(directory, fileName)
    }

    fun getAllPhotos(): List<File> {
        val directory = context.getExternalFilesDir("photos")
        return directory?.listFiles()?.toList() ?: emptyList()
    }
}
