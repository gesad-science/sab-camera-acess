package ui.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sab.cameraacess.R
import face.FaceDetectorManager
import helpers.ApiConfigManager
import helpers.MARGIN_TOP_POPUP
import photos.PhotoManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val message = intent.getStringExtra("snackbar_message")
        val function = intent.getStringExtra("function")
        val userName = intent.getStringExtra("username") ?: "User"
        textWelcome.text = getString(R.string.welcome_user, userName)
        onBackPressedDispatcher
            .addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Ignore back button
                    }
                },
            )
        val textLogout = findViewById<TextView>(R.id.textLogout)
        setupLogoutText(textLogout)
        val buttonRollCall = findViewById<Button>(R.id.buttonRollCall)
        message?.let {
            showSnackbar(it)
            intent.removeExtra("snackbar_message")
        }
        if (function == "Administrator") {
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
                saveApiConfig(inputApiUrl, radioGroupModel, apiConfigManager, dialog)
            }
            dialog.show()
        }
        findViewById<Button>(R.id.buttonCamera).setOnClickListener {
            val apiUrl = apiConfigManager.getFinalUrl()
            val modelName = apiConfigManager.getModelName()
            if (isApiConfigValid()) return@setOnClickListener
            val intent =
                Intent(this, CameraActivity::class.java)
                    .apply {
                        putExtra("api_url", apiUrl)
                        putExtra("model_name", modelName)
                        putExtra("function", function)
                        putExtra("username", userName)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    }
            startActivity(intent)
        }
    }

    private fun isApiConfigValid(): Boolean {
        val apiUrl = apiConfigManager.getFinalUrl()
        val modelName = apiConfigManager.getModelName()
        return if (apiUrl.isEmpty() || modelName.isEmpty()) {
            showSnackbar("Please configure the API URL and model first")
            false
        } else {
            true
        }
    }

    private fun saveApiConfig(
        inputApiUrl: EditText,
        radioGroupModel: RadioGroup,
        apiConfigManager: ApiConfigManager,
        dialog: Dialog,
    ) {
        val url = inputApiUrl.text.toString()
        val selectedRadioId = radioGroupModel.checkedRadioButtonId

        if (url.isEmpty() || selectedRadioId == -1) {
            showSnackbar("Fill in all the fields")
            return
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

    private fun setupLogoutText(textView: TextView) {
        textView.apply {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            setTextColor(Color.BLUE)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
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
            params.topMargin = MARGIN_TOP_POPUP
            view.layoutParams = params
            snackbar.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
