package helpers

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import files.LogHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

private const val LIMIT_JSON = 200

object FaceApiHelper {
    private fun Bitmap.toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, QUALITY_CAMERA, outputStream)
        return outputStream.toByteArray()
    }

    fun sendFacesToApi(
        context: Context,
        faces: List<Bitmap>,
        url: String,
    ) {
        if (faces.isEmpty()) {
            LogHelper.log(context, "No faces detected for send.")
            return
        }

        val client = OkHttpClient()

        faces.forEachIndexed { index, bitmap ->
            try {
                val byteArray = bitmap.toByteArray()
                val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                val json = """{"image_base64":"$base64"}"""
                LogHelper.log(context, "Sending face #$index: ${json.take(LIMIT_JSON)}...")
                val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request =
                    Request
                        .Builder()
                        .url(url)
                        .post(body)
                        .build()

                client.newCall(request).enqueue(
                    object : Callback {
                        override fun onFailure(
                            call: Call,
                            e: IOException,
                        ) {
                            LogHelper.log(context, "Error sending face #$index: ${e.localizedMessage}")
                        }

                        override fun onResponse(
                            call: Call,
                            response: Response,
                        ) {
                            response.use {
                                if (!response.isSuccessful) {
                                    LogHelper.log(
                                        context,
                                        "Error of API (${response.code}) for face #$index: ${response.message}",
                                    )
                                    return
                                }
                                LogHelper.log(context, "Face #$index sent successfully! Code: ${response.code}")
                            }
                        }
                    },
                )
            } catch (e: IOException) {
                LogHelper.log(context, "I/O error preparing face #$index: ${e.localizedMessage}")
            } catch (e: IllegalArgumentException) {
                LogHelper.log(context, "Encoding error for face #$index: ${e.localizedMessage}")
            } catch (e: IllegalStateException) {
                LogHelper.log(context, "State error for face #$index: ${e.localizedMessage}")
            }
        }
    }
}
