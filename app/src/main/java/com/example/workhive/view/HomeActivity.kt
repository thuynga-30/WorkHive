package com.example.workhive.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R
import com.example.workhive.adapter.ToDoAdapter
import com.example.workhive.api.RetrofitTask
import com.example.workhive.helper.BottomNavHelper
import com.example.workhive.databinding.ActivityMainBinding
import com.example.workhive.databinding.CardTaskDoingBinding
import com.example.workhive.databinding.CardTaskDoneBinding
import com.example.workhive.databinding.CardTaskOverdueBinding
import com.example.workhive.databinding.CardTaskPendingBinding
import com.example.workhive.model.Task
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cardoneBinding: CardTaskDoneBinding
    private lateinit var cardoingBinding: CardTaskDoingBinding
    private lateinit var cardpendingBinding: CardTaskPendingBinding
    private lateinit var cardoverBinding: CardTaskOverdueBinding
    private lateinit var todoAdapter: ToDoAdapter
    private val todos = mutableListOf<Task>()
    private val upcomingTasks = mutableListOf<Task>()
    private lateinit var upcomingAdapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set the content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the ViewBinding for the included layouts
        val cardDoneView = findViewById<View>(R.id.cardTaskDone) // container là 1 FrameLayout
        cardoneBinding = CardTaskDoneBinding.bind(cardDoneView)
        val cardDoingView = findViewById<View>(R.id.cardTaskDoing) // container là 1 FrameLayout
        cardoingBinding = CardTaskDoingBinding.bind(cardDoingView)  // Lấy ViewBinding cho layout card_task_doing
        val cardPendingView = findViewById<View>(R.id.cardTaskPending) // container là 1 FrameLayout
        cardpendingBinding = CardTaskPendingBinding.bind(cardPendingView)  // Lấy ViewBinding cho layout card_task_pending
        val cardOverView = findViewById<View>(R.id.cardTaskOverdue) // container là 1 FrameLayout
        cardoverBinding = CardTaskOverdueBinding.bind(cardOverView)  // Lấy ViewBinding cho layout card_task_overdue

        // Call function to count task statuses


        // Setup Bottom Navigation
        BottomNavHelper.setupBottom(this, R.id.menu_home)

        // Initialize the adapters for both todo tasks and upcoming deadlines
        todoAdapter = ToDoAdapter(todos, {
            countTaskStatuses(todos)
        }) { updatedTask ->
            // Cập nhật lại giao diện nếu task cũng thuộc upcoming
            if (upcomingTasks.contains(updatedTask)) {
                upcomingAdapter.notifyDataSetChanged()
            }
        }
        upcomingAdapter = ToDoAdapter(upcomingTasks, {
            countTaskStatuses(todos)
        }) { updatedTask ->
            // Cập nhật lại giao diện nếu task cũng thuộc todos
            todoAdapter.notifyDataSetChanged()
        }
        // Set up RecyclerViews
        binding.recyclerTodoTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerTodoTasks.adapter = todoAdapter

        binding.recyclerUpcomingDeadlines.layoutManager = LinearLayoutManager(this)
        binding.recyclerUpcomingDeadlines.adapter = upcomingAdapter
        // Load data from the API
        loadData()
        checkOverTask()
    }


    @SuppressLint("NewApi")
    private fun loadData() {
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        Log.d("DEBUG_HOME", "Loading data for user: $userName")
        lifecycleScope.launch {
            try {
                val response = RetrofitTask.taskApi.getTaskByUser(userName)

                Log.d("DEBUG_HOME", "API response: ${response.success}")
                Log.d("DEBUG_HOME", "API message: ${response.message}")

                if (response.success) {
                    todos.clear()
                    upcomingTasks.clear()

                    val allTasks = response.tasks ?: emptyList()
                    Log.d("DEBUG_HOME", "Received tasks: ${allTasks.size}")
                    todos.addAll(allTasks)
                    val today = LocalDate.now() // Lấy ngày hiện tại
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    for (task in allTasks) {
                        try {
                            val dueDate = LocalDate.parse(task.due_date, formatter)
                            // Kiểm tra nếu quá hạn và cập nhật trạng thái task
                            if (dueDate.isBefore(today)) {
                                task.status = "Over Due" // Cập nhật trạng thái thành Over Due
                            }
                            // Kiểm tra nếu task gần hết hạn và đưa vào danh sách upcomingTasks
                            if (dueDate == LocalDate.now() ||dueDate.isAfter(today) && dueDate.isBefore(today.plusDays(4))) {
                                upcomingTasks.add(task)
                            }
                        } catch (e: Exception) {
                            Log.e("PARSE_ERROR", "Error parsing date: ${task.due_date} - ${e.message}")
                        }
                    }

                    // Sau khi cập nhật trạng thái, gọi lại countTaskStatuses để tính lại số liệu
                    countTaskStatuses(todos)

                    Log.d("DEBUG_HOME", "Upcoming tasks: ${upcomingTasks.size}")
                    todoAdapter.notifyDataSetChanged() // Cập nhật RecyclerView
                    upcomingAdapter.notifyDataSetChanged() // Cập nhật RecyclerView

                } else {
                    Log.d("DEBUG_HOME", "API Error: ${response.message}")
                    Toast.makeText(this@HomeActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Error loading task list: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("DEBUG_HOME", "Exception: ${e.message}")
            }
        }
    }


    fun countTaskStatuses(tasks: List<Task>) {
        var completedCount = 0
        var inProgressCount = 0
        var pendingCount = 0
        var overdueCount = 0

        for (task in tasks) {
            when (task.status) {
                "Done" -> completedCount++
                "Doing" -> inProgressCount++
                "Pending" -> pendingCount++
                "Over Due" -> overdueCount++
            }
        }

        Log.d("Task Status Count", "Completed: $completedCount")
        Log.d("Task Status Count", "In Progress: $inProgressCount")
        Log.d("Task Status Count", "Pending: $pendingCount")
        Log.d("Task Status Count", "Over Due: $overdueCount")
        cardoneBinding.completedCount.text = "$completedCount"
        cardoingBinding.inProgressCount.text = "$inProgressCount"
        cardpendingBinding.pendingCount.text = "$pendingCount"
        cardoverBinding.overdueCount.text = "$overdueCount"

    }
    private fun checkOverTask() {
        RetrofitTask.taskApi.checkOver().enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("CheckOverdue", "✅ Đã quét công việc quá hạn")
                } else {
                    Log.e("CheckOverdue", "❌ Server trả về lỗi")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("CheckOverdue", "❌ Lỗi kết nối: ${t.message}")
            }
        })
    }


}
