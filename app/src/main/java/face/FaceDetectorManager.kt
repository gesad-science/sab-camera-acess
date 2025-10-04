package face

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

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
        input: InputImage,
        onResult: (List<Face>) -> Unit,
    ) {
        detector
            .process(input)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    onResult(faces)
                } else {
                    Log.w("FaceDetectorManager", "No face detected")
                    onResult(emptyList())
                }
            }.addOnFailureListener { e ->
                Log.e("FaceDetectorManager", "Detection error", e)
                onResult(emptyList())
            }
    }
}
