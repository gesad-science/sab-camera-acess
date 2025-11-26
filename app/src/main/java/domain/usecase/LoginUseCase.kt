package domain.usecase

import repository.UserRepository

class LoginUseCase(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        function: String,
    ) = repository.login(username, password, function)
}
