package face

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectorManager {
    private val options =
        FaceDetectorOptions
            .Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

    private val detector = FaceDetection.getClient(options)

    fun detectFaces(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        detector
            .process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    face.boundingBox
                } else {
                    Log.w("FaceDetectorManager", "No face detected")
                }
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
