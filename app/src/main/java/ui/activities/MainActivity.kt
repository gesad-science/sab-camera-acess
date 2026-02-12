package ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.sab.cameraacess.R
import database.UserDatabase
import domain.usecase.LoginUseCase
import factory.LoginViewModelFactory
import helpers.MARGIN_TOP_POPUP
import kotlinx.coroutines.launch
import repository.UserRepository
import ui.viewsmodel.LoginViewModel

class MainActivity : AppCompatActivity() {
    private var loginDialog: AlertDialog? = null
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            LoginUseCase(
                UserRepository(
                    UserDatabase.getDatabase(this).userDao(),
                ),
            ),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch {
            UserRepository(
                UserDatabase.getDatabase(this@MainActivity).userDao(),
            ).registerDefaultUser()
        }

        val button = findViewById<Button>(R.id.buttonLogin)
        button.setOnClickListener {
            showLoginDialog()
        }
    }

    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        val edtUser = dialogView.findViewById<EditText>(R.id.edtUser)
        val edtPass = dialogView.findViewById<EditText>(R.id.edtPassword)
        val edtFun = dialogView.findViewById<RadioGroup>(R.id.radioGroupTypeUser)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        loginDialog =
            AlertDialog
                .Builder(this)
                .setView(dialogView)
                .create()

        loginDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loginDialog?.show()

        btnConfirm.setOnClickListener {
            val typeUser = validateLoginInputs(edtUser, edtPass, edtFun)

            if (typeUser != null) {
                val username = edtUser.text.toString().trim()
                viewModel.login(
                    username,
                    edtPass.text.toString(),
                    typeUser,
                )
                observeLoginState(username)
            }
        }
    }

    private fun validateLoginInputs(
        edtUser: EditText,
        edtPass: EditText,
        edtFun: RadioGroup,
    ): String? {
        val password = edtPass.text.toString()
        val username = edtUser.text.toString().trim()
        val selectedFunction = edtFun.checkedRadioButtonId

        if (username.isEmpty() || password.isEmpty() || selectedFunction == -1) {
            showSnackbar("Fill in all the fields")
            return null
        }

        return when (selectedFunction) {
            R.id.radioAdministrator -> "Administrator"
            R.id.radioUser -> "User"
            else -> null
        }
    }

    private fun observeLoginState(username: String) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginViewModel.LoginState.Success -> {
                            loginDialog?.dismiss()
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("function", state.function)
                            intent.putExtra("username", username)
                            startActivity(intent)
                        }
                        is LoginViewModel.LoginState.Error -> {
                            showSnackbar("User or password or user type invalidates")
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, duration)
        val view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = MARGIN_TOP_POPUP
        view.layoutParams = params
        snackbar.show()
    }
}
