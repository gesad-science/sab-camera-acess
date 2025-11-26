package factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import domain.usecase.LoginUseCase
import ui.viewsmodel.LoginViewModel

class LoginViewModelFactory(
    private val useCase: LoginUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel(useCase) as T
}
