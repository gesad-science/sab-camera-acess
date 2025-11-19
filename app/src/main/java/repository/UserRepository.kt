package repository

import dao.UserDao
import model.User

class UserRepository(private val dao: UserDao) {

    suspend fun login(username: String, password: String, function: String): User? {
        return dao.login(username, password, function)
    }

    suspend fun registerDefaultUser() {
        dao.insert(User(username = "Paulo Henrique", password = "123321", function = "Administrator"))
        dao.insert(User(username = "Guilherme Gaspar", password = "45654", function = "User"))
    }

}
