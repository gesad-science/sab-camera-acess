package ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sab.cameraacess.R
import kotlinx.coroutines.launch
import ui.viewsmodel.PresenceViewModel
import ui.viewsmodel.StudentAdapter

class RollCallActivity : AppCompatActivity() {
    private lateinit var presenceViewModel: PresenceViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var function: String
    private lateinit var userName: String
    private val adapter = StudentAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_call)
        function = intent.getStringExtra("function") ?: ""
        userName = intent.getStringExtra("username") ?: "User"
        recyclerView = findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        presenceViewModel = ViewModelProvider(this)[PresenceViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                presenceViewModel.students.collect { list ->
                    adapter.updateList(list)
                }
            }
        }
        presenceViewModel.loadPresences()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.buttonBackRoll).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("function", function)
            intent.putExtra("username", userName)
            startActivity(intent)
        }
    }
}
