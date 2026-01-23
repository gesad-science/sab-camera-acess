package ui.activities

import android.app.Dialog
import android.content.Intent
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
import api.ApiConfigManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sab.cameraacess.R
import face.FaceDetectorManager
import helpers.MARGIN_TOP_POPUP
import photos.PhotoManager
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeActivity : AppCompatActivity() {
    private lateinit var photoManager: PhotoManager
    private lateinit var faceDetectorManager: FaceDetectorManager
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var apiConfigManager: ApiConfigManager
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        photoManager = PhotoManager(this)
        faceDetectorManager = FaceDetectorManager()
        cameraExecutor = Executors.newSingleThreadExecutor()
        apiConfigManager = ApiConfigManager(this)
        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val userName = intent.getStringExtra("username") ?: "User"
        textWelcome.text = getString(R.string.welcome_user, userName)
        textDate = findViewById(R.id.textDate)
        textTime = findViewById(R.id.textTime)
        updateDateTimeRunnable.run()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Ignore back button
                }
            },
        )
        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        intent.getStringExtra("snackbar_message")?.let {
            showSnackbar(it)
            intent.removeExtra("snackbar_message")
        }

        setupVisibility()
        setupConfigurationButton()
        setupCameraButton()
        setupRollCallButton()
    }

    private fun setupVisibility() {
        val buttonRollCall = findViewById<Button>(R.id.buttonRollCall)
        val function = intent.getStringExtra("function")
        buttonRollCall.visibility =
            if (function == "Administrator") View.VISIBLE else View.GONE
    }

    private fun setupConfigurationButton() {
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
    }

    private fun setupCameraButton() {
        findViewById<Button>(R.id.buttonCamera).setOnClickListener {
            val apiUrl = apiConfigManager.getFinalUrl()
            val modelName = apiConfigManager.getModelName()
            if (!isApiConfigValid()) return@setOnClickListener

            val intent =
                Intent(this, CameraActivity::class.java).apply {
                    putExtra("api_url", apiUrl)
                    putExtra("model_name", modelName)
                    putExtra("function", intent.getStringExtra("function"))
                    putExtra("username", intent.getStringExtra("username") ?: "User")
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                }
            startActivity(intent)
        }
    }

    private fun setupRollCallButton() {
        val buttonRollCall = findViewById<Button>(R.id.buttonRollCall)
        buttonRollCall.setOnClickListener {
            val intent =
                Intent(this, RollCallActivity::class.java).apply {
                    putExtra("function", intent.getStringExtra("function"))
                    putExtra("username", intent.getStringExtra("username") ?: "User")
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

    private val updateDateTimeRunnable = object : Runnable {
        override fun run() {
            val timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
            val calendar = Calendar.getInstance(timeZone)
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            dateFormatter.timeZone = timeZone
            timeFormatter.timeZone = timeZone
            val now = calendar.time
            textDate.text = dateFormatter.format(now)
            textTime.text = timeFormatter.format(now)
            handler.postDelayed(this, 60_000)
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
        handler.removeCallbacks(updateDateTimeRunnable)
        cameraExecutor.shutdown()
    }
}
