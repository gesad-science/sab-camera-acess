package ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sab.cameraacess.R
import face.FaceDetectorManager
import helpers.ApiConfigManager
import photos.PhotoManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val MARGIN_TOP = 80

class HomeActivity : AppCompatActivity() {
    private lateinit var photoManager: PhotoManager
    private lateinit var faceDetectorManager: FaceDetectorManager
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var apiConfigManager: ApiConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        photoManager = PhotoManager(this)
        faceDetectorManager = FaceDetectorManager()
        cameraExecutor = Executors.newSingleThreadExecutor()
        apiConfigManager = ApiConfigManager(this)
        val message = intent.getStringExtra("snackbar_message")
        val function = intent.getStringExtra("function")
        val buttonRollCall = findViewById<Button>(R.id.buttonRollCall)
        message?.let {
            showSnackbar(it)
            intent.removeExtra("snackbar_message")
        }
        if(function == "Administrator") {
            buttonRollCall.visibility = View.VISIBLE
        } else {
            buttonRollCall.visibility = View.GONE
        }
        findViewById<ImageView>(R.id.buttonConfiguration).setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.layout_configuration_bottomsheet, null)
            dialog.setContentView(view)

            val inputApiUrl = view.findViewById<EditText>(R.id.inputApiUrl)
            val radioGroupModel = view.findViewById<RadioGroup>(R.id.radioGroupModel)
            val buttonSave = view.findViewById<Button>(R.id.buttonSaveConfig)
            buttonSave.setOnClickListener {
                val url = inputApiUrl.text.toString()
                val selectedRadioId = radioGroupModel.checkedRadioButtonId

                if (url.isEmpty() || selectedRadioId == -1) {
                    showSnackbar("Fill in all the fields")
                    return@setOnClickListener
                }
                val model =
                    when (selectedRadioId) {
                        R.id.radioResNet -> "ResNet"
                        R.id.radioMobileNet -> "MobileNet"
                        else -> ""
                    }
                apiConfigManager.setConfig(url, model)
                showSnackbar("Configuration Saved")
                dialog.dismiss()
            }
            dialog.show()
        }
        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<Button>(R.id.buttonCamera).setOnClickListener {
            val apiUrl = apiConfigManager.getFinalUrl()
            val modelName = apiConfigManager.getModelName()
            if (apiUrl.isEmpty() || modelName.isEmpty()) {
                showSnackbar("Please configure the API URL and model first")
                return@setOnClickListener
            }

            val intent =
                Intent(this, CameraActivity::class.java)
                    .apply {
                        putExtra("api_url", apiUrl)
                        putExtra("model_name", modelName)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    }
            startActivity(intent)
        }
    }

    private fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
    ) {
        runOnUiThread {
            val snackbar =
                Snackbar.make(
                    findViewById(android.R.id.content),
                    message,
                    duration,
                )
            val view = snackbar.view
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.topMargin = MARGIN_TOP
            view.layoutParams = params
            snackbar.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
