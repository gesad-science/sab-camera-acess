package ui.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import model.Student
import retrofit2.HttpException
import java.io.IOException

class PresenceViewModel : ViewModel() {
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students

    fun loadPresences() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getPresences()

                val presentStudents =
                    response.records
                        ?.mapNotNull { it.faceName }
                        ?.distinct()
                        ?: emptyList()

                val allStudents =
                    listOf(
                        Student("Camila Pinheiro", "12341", false),
                        Student("David Moreira", "67892", false),
                        Student("Paulo Henrique Maia", "11224", false),
                        Student("Ana Oliveira", "44556", false),
                    )

                _students.value =
                    allStudents.map { student ->
                        student.copy(present = presentStudents.contains(student.name))
                    }
            } catch (e: IOException) {
                e.printStackTrace()
                _students.value = emptyList()
            } catch (e: HttpException) {
                e.printStackTrace()
                _students.value = emptyList()
            }
        }
    }

    fun loadMock() {
        val mockStudents =
            listOf(
                Student("Alan Bandeira", "12341", true),
                Student("David Peacock", "67892", false),
                Student("Paulo Henrique Maia", "11224", true),
                Student("Camila", "44554", false),
                Student("João Silva", "12345", true),
                Student("Maria Souza", "67890", false),
                Student("Pedro Santos", "11223", true),
                Student("Ana Oliveira", "44556", false),
                Student("Paulo Henrique Maia", "11224", true),
                Student("Camila", "44554", false),
                Student("João Silva", "12345", true),
                Student("Maria Souza", "67890", false),
                Student("Pedro Santos", "11223", true),
                Student("Ana Oliveira", "44556", false),
            )
        _students.value = mockStudents
    }
}
