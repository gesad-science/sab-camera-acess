package ui.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import domain.usecase.LoginUseCase

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String, function: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = loginUseCase(username, password, function)

            if (result != null && result.password == password && result.function == function) {
                _loginState.value = LoginState.Success(function)
            } else {
                _loginState.value = LoginState.Error("User, password or type user invalidate")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val function: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
