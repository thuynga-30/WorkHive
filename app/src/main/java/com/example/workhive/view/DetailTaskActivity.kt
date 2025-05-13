package com.example.workhive.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R
import com.example.workhive.adapter.AssignToDialog
import com.example.workhive.adapter.DetailTaskAdapter
import com.example.workhive.api.RetrofitTask
import com.example.workhive.databinding.DetailTaskBinding
import com.example.workhive.helper.BottomNavHelper
import com.example.workhive.model.GetTasksResponse
import com.example.workhive.model.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: DetailTaskBinding
    private lateinit var task: Task
    private lateinit var team :Group
    private var taskList: MutableList<Task> = mutableListOf()
    private lateinit var adapter: DetailTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        BottomNavHelper.setupBottom(this, R.id.menu_home )

        // Lấy dữ liệu từ Intent
        val groupId = intent.getIntExtra("GROUP_ID", -1)
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: ""
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION") ?: ""
        val taskAssigned = intent.getStringExtra("TASK_ASSIGNED") ?: ""
        val taskStatus = intent.getStringExtra("TASK_STATUS") ?: ""
        val taskDuedate = intent.getStringExtra("TASK_DUE_DATE") ?: ""
        val taskParent = intent.getIntExtra("TASK_PARENT_ID", -1)
        val groupCreatedBy = intent.getStringExtra("GROUP_CREATED_BY") ?: ""
        // Tạo đối tượng Task
        task = Task(
            taskId,
            taskTitle,
            taskDescription,
            taskAssigned,
            groupId,
            taskStatus,
            taskDuedate,
            taskParent
        )

        // Gán dữ liệu vào giao diện
        binding.taskName.text = taskTitle
        binding.taskDescription.text = taskDescription

        // Set RecyclerView
        adapter = DetailTaskAdapter(taskList)
        binding.taskRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.taskRecyclerView.adapter = adapter

        // Gọi API lấy subtask
        if (groupId != -1 && taskId != -1) {
            loadSubTask(taskId, groupId)
            updateProgress(taskId)
        } else {
            Toast.makeText(this, "Thiếu thông tin Group hoặc Task", Toast.LENGTH_SHORT).show()
        }
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val isLeader = (groupCreatedBy == userName)

        binding.removeButton.visibility = if (isLeader) View.VISIBLE else View.GONE
        binding.addButton.visibility = if (isLeader) View.VISIBLE else View.GONE
        binding.addButton.setOnClickListener {
            showAddSubTask(task)
        }
        binding.removeButton.setOnClickListener {
            showDeleteDialog()
        }


    }
    private fun updateProgress(taskId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitTask.taskApi.getTaskProgress(taskId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val percent = response.body()?.progress_percent ?: 0
                    binding.taskProgress.setProgressCompat(percent, true)
                    binding.progressText.text = "$percent%"
                    if (percent == 100 && task.status != "Done") {
                        updateTaskStatus(taskId, "Done")
                    }
                } else {
                    Toast.makeText(this@DetailTaskActivity, "Không cập nhật được tiến độ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailTaskActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTaskStatus(taskId: Int, status: String) {
        lifecycleScope.launch {
            try {
                val updateRequest = UpdateRequest(taskId, status)
                val response = RetrofitTask.taskApi.updateStatus(updateRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@DetailTaskActivity, "Task chính đã hoàn thành", Toast.LENGTH_SHORT).show()
                    task = task.copy(status = status) // Cập nhật lại trạng thái hiện tại trong biến task
                } else {
                    Toast.makeText(this@DetailTaskActivity, "Cập nhật trạng thái thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailTaskActivity, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showDeleteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_member, null)
        val inputEditText = dialogView.findViewById<android.widget.EditText>(R.id.inputMemberName)
        val titleText = dialogView.findViewById<android.widget.TextView>(R.id.dialogMessage)
        inputEditText.hint =" Enter subtask title"
        titleText.text= "Enter the subtask title to delete:"
        AlertDialog.Builder(this)
            .setTitle("Delete sub task from Task ")
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                val title = inputEditText.text.toString().trim()
                if (title.isNotEmpty()) {
                    deleteTask(title)

                } else {
                    Toast.makeText(this, "Please enter the title ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancle", null)
            .show()
    }

    private fun deleteTask(title: String) {
        val request = DeleteRequest(title)
        RetrofitTask.taskApi.deleteSubTask(request).enqueue(object : Callback<getResponse>{
            override fun onResponse(call: Call<getResponse>, response: Response<getResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@DetailTaskActivity, "Đã xóa ", Toast.LENGTH_SHORT).show()
                    loadSubTask(task.task_id, task.group_id)
                    updateProgress(task.task_id)
                } else {
                    Toast.makeText(this@DetailTaskActivity, response.body()?.message ?: "Thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<getResponse>, t: Throwable) {
                Toast.makeText(this@DetailTaskActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }


    private fun showAddSubTask(task: Task) {
        val dialog = AssignToDialog(task.task_id, task.group_id) {
            loadSubTask(task.task_id, task.group_id)
            updateProgress(task.task_id)
        }
        dialog.show(supportFragmentManager, "AssignToDialog")
    }

    @SuppressLint("NewApi")
    private fun loadSubTask(taskId: Int, groupId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitTask.taskApi.getSubTask(groupId, taskId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tasks = response.body()?.tasks ?: emptyList()
                    taskList.clear()
                    taskList.addAll(tasks)
                    val today = LocalDate.now() // Lấy ngày hiện tại
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                    for (task in tasks) {
                        try {
                            val dueDate = LocalDate.parse(task.due_date, formatter)

                            // Kiểm tra nếu quá hạn và cập nhật trạng thái task
                            if (dueDate.isBefore(today)) {
                                task.status = "Over Due" // Cập nhật trạng thái thành Over Due
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "PARSE_ERROR",
                                "Error parsing date: ${task.due_date} - ${e.message}"
                            )

                        }
                    }
                            adapter.notifyDataSetChanged()
                } else {
                    Log.e("DEBUG_API", "API lỗi: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_API", "Lỗi mạng: ${e.message}")
                Toast.makeText(this@DetailTaskActivity, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
