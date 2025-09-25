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
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
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
                    Log.w("FaceDetectorManager", "Nenhum rosto detectado")
                    onResult(emptyList())
                }
            }.addOnFailureListener { e ->
                Log.e("FaceDetectorManager", "Erro na detecção", e)
                onResult(emptyList())
            }
    }
}
