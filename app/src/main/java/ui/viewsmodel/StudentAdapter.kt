package ui.viewsmodel

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sab.cameraacess.R
import model.Student

class StudentAdapter(
    private var students: List<Student>,
) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {
    class ViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<TextView>(R.id.textName)
        private val registration = view.findViewById<TextView>(R.id.textRegistration)

        private val status = view.findViewById<TextView>(R.id.textStatus)

        fun bind(student: Student) {
            name.text = student.name
            registration.text = student.registration
            status.text = if (student.present) "Present" else "Absent"
            status.setTextColor(if (student.present) Color.GREEN else Color.RED)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Student>) {
        this.students = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(students[position])
    }

    override fun getItemCount() = students.size
}
