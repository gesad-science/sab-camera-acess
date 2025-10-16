package files

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object LogHelper {
    private const val LOG_TAG = "FaceApp"
    private const val FILE_NAME = "app_log.txt"

    fun log(
        context: Context,
        message: String,
    ) {
        Log.d(LOG_TAG, message)
        saveLog(
            context,
            message,
        )
    }

    private fun saveLog(
        context: Context,
        message: String,
    ) {
        try {
            getOutputStream(context)?.use { stream ->
                writeLog(stream, message)
            }
            Log.d(LOG_TAG, "Log saved in $FILE_NAME")
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error in save log: ${e.message}")
        }
    }

    private fun getOutputStream(context: Context): OutputStream? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getScopedOutputStream(context)
        } else {
            getLegacyOutputStream(context)
        }

    @RequiresApi(
        Build.VERSION_CODES.Q,
    )
    private fun getScopedOutputStream(context: Context): OutputStream? {
        val resolver = context.contentResolver
        val existingFile =
            resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.Downloads.DISPLAY_NAME}=?",
                arrayOf(FILE_NAME),
                null,
            )

        val uri =
            if (existingFile != null && existingFile.moveToFirst()) {
                val id = existingFile.getLong(existingFile.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                existingFile.close()
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(id.toString())
                    .build()
            } else {
                existingFile?.close()
                val contentValues =
                    ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
                        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            }

        return uri?.let { resolver.openOutputStream(it, "wa") }
    }

    private fun getLegacyOutputStream(context: Context): OutputStream? {
        val privateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (privateDir != null && !privateDir.exists()) privateDir.mkdirs()
        val file = File(privateDir, FILE_NAME)
        return FileOutputStream(file, true)
    }

    private fun writeLog(
        stream: OutputStream,
        message: String,
    ) = stream.use {
        it.write("${System.currentTimeMillis()} - $message\n".toByteArray())
        it.flush()
    }
}
