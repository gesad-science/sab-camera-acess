package files

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
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

    fun log(context: Context, message: String) {
        Log.d(LOG_TAG, message)
        saveLog(context, message)
    }

    private fun saveLog(context: Context, message: String) {
        try {
            getOutputStream(context)?.use { stream ->
                writeLog(stream, message)
            }
            Log.d(LOG_TAG, "Log salvo com sucesso.")
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Erro ao salvar log: ${e.message}")
        }
    }

    private fun getOutputStream(context: Context): OutputStream? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getScopedOutputStream(context)
        } else {
            getLegacyOutputStream(context)
        }

    /**
     * Usa MediaStore para salvar SEM recriar o arquivo.
     * Se o arquivo já existir, reabre para append ("wa").
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getScopedOutputStream(context: Context): OutputStream? {
        val resolver = context.contentResolver
        var uri: Uri? = null

        // 1. Tenta achar o arquivo existente
        resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID),
            "${MediaStore.Downloads.DISPLAY_NAME}=?",
            arrayOf(FILE_NAME),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
            }
        }

        // 2. Se não encontrou, cria novo
        if (uri == null) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        }

        return uri?.let { resolver.openOutputStream(it, "wa") }
    }

    private fun getLegacyOutputStream(context: Context): OutputStream? {
        val privateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (privateDir != null && !privateDir.exists()) privateDir.mkdirs()
        val file = File(privateDir, FILE_NAME)
        return FileOutputStream(file, true)
    }

    private fun writeLog(stream: OutputStream, message: String) {
        stream.use {
            it.write("${System.currentTimeMillis()} - $message\n".toByteArray())
            it.flush()
        }
    }
}
