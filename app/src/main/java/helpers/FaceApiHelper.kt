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
import org.json.JSONException
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
        try {
            val jsonFaces =
                faces.map { bitmap ->
                    val byteArray = bitmap.toByteArray()
                    Base64.encodeToString(byteArray, Base64.NO_WRAP)
                }
            val json = """{"faces":[${jsonFaces.joinToString(",") { "\"$it\"" }}]}"""
            LogHelper.log(context, "Send JSON: ${json.take(LIMIT_JSON)}...")
            val client = OkHttpClient()
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
                        LogHelper.log(context, "Error to connect for API: ${e.localizedMessage}")
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        response.use {
                            if (!response.isSuccessful) {
                                LogHelper.log(context, "Error of API (${response.code}): ${response.message}")
                                return
                            }
                            LogHelper.log(context, "Faces sent! Code: ${response.code}")
                        }
                    }
                },
            )
        } catch (e: JSONException) {
            LogHelper.log(context, "Error to make JSON: ${e.localizedMessage}")
        } catch (e: IllegalArgumentException) {
            LogHelper.log(context, "Error to encode image: ${e.localizedMessage}")
        } catch (e: IllegalStateException) {
            LogHelper.log(context, "State error: ${e.localizedMessage}")
        }
    }
}
