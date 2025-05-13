package com.example.workhive.adapter

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.workhive.R
import com.example.workhive.api.RetrofitTask
import com.example.workhive.databinding.ItemTaskBinding
import com.example.workhive.model.*
import com.example.workhive.view.DetailTaskActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskAdapter(
    private val team :Group,
    private val taskList: MutableList<Task>,
    private val onGroupClicked: (Task) -> Unit, // <- Thêm callback này
    private val onGroupDelete : (Task)-> Unit
): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = taskList.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                tvTaskTitle.text = task.title
                tvTaskDesc.text = task.description
                tvTaskDate.text = task.due_date
                tvTaskStatus.text = task.status
                if (task.status == "Over Due") {
                    tvTaskStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                }
                val sharedPref = binding.root.context.getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                val userName = sharedPref.getString("USER_NAME", "") ?: ""

                removeTaskButton.visibility = if (team.created_by == userName) View.VISIBLE else View.GONE
                removeTaskButton.setOnClickListener {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Delete") { _, _ ->
                            requestDelete(task)
                        }
                        .setNegativeButton("Cancle", null)
                        .show()
                }
                detailTask.setOnClickListener {
                    showDetailTask(task)
                }
                updateButton.visibility = if (team.created_by == userName) View.VISIBLE else View.GONE
                updateButton.setOnClickListener {
                    showUpdateDialog(task)
                }
                root.setOnClickListener {
                    onGroupClicked(task) // <- Gọi callback khi click vào item
                }
            }
        }
        fun updateTask(updatedTask: Task, position: Int) {
            taskList[position] = updatedTask
            notifyItemChanged(position)
        }
        private fun showUpdateDialog(task: Task) {
            val dialog = UpdateTaskDialog(
                task.task_id,
                currentName = task.title,
                currentDesc = task.description,
                currentDate = task.due_date
            ){newTitle, newDesc, newDate ->
                val updatedTask = task.copy(
                    title = newTitle,
                    description = newDesc,
                    due_date = newDate)

            updateTask(updatedTask, position)
            }
            dialog.show(
                (binding.root.context as AppCompatActivity).supportFragmentManager,
                "UpdateGroupDialog"
            )
        }


        private fun showDetailTask(task: Task) {
            val intent = Intent(itemView.context, DetailTaskActivity::class.java).apply {
                putExtra("TASK_ID", task.task_id)
                putExtra("GROUP_ID", task.group_id)
                putExtra("TASK_TITLE", task.title)
                putExtra("TASK_DESCRIPTION", task.description)
                putExtra("TASK_ASSIGNED", task.assigned_to)
                putExtra("TASK_STATUS", task.status)
                putExtra("TASK_DUE_DATE", task.due_date)
                putExtra("TASK_PARENT_ID", task.parent_id)
                putExtra("GROUP_CREATED_BY", team.created_by)

            }
            itemView.context.startActivity(intent)
        }

        private fun requestDelete(task: Task) {
            val context = binding.root.context
            val sharedPref = context.getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            val userName = sharedPref.getString("USER_NAME", "") ?: ""
            if (userName.isEmpty()) {
                Toast.makeText(context, "User not logged in. Please log in.", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            val request = RemoveTaskRequest(task.task_id, task.group_id)

            RetrofitTask.taskApi.removeTask(request).enqueue(object :
                Callback<getResponse> {
                override fun onResponse(call: Call<getResponse>, response: Response<getResponse>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.success == true) {
                            Toast.makeText(context, "Delete Task successfully", Toast.LENGTH_SHORT)
                                .show()
                            onGroupDelete(task)
                        } else {
                            Log.e("DeleteTask", "Failed: ${responseBody?.message}")
                            Toast.makeText(
                                context,
                                responseBody?.message ?: "Failed to delete",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e(
                            "DeleteTask",
                            "Server Error: ${response.code()} - ${response.message()}"
                        )
                        Toast.makeText(
                            context,
                            "Server Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<getResponse>, t: Throwable) {
                    Log.e("DeleteTask", "Network Error: ${t.message}")
                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Failed to connect to server. Please check your network connection.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            })
        }
    }
}

