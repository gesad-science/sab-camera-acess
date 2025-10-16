package face

import android.content.Context
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import files.LogHelper

class FaceDetectorManager {
    private val options =
        FaceDetectorOptions
            .Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

    private val detector = FaceDetection.getClient(options)

    fun detectFaces(
        context: Context,
        input: InputImage,
        onResult: (List<Face>) -> Unit,
    ) {
        detector
            .process(input)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    onResult(faces)
                } else {
                    LogHelper.log(context, "No face detected")
                    onResult(emptyList())
                }
            }.addOnFailureListener { e ->
                LogHelper.log(context, "Detection error: ${e.message ?: "Unknown error"}")
                e.stackTrace.forEach {
                    LogHelper.log(context, it.toString())
                }
                onResult(emptyList())
            }
    }
}
