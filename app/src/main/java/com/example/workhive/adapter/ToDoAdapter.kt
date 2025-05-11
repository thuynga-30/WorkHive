package com.example.workhive.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.workhive.R
import com.example.workhive.api.RetrofitTask
import com.example.workhive.databinding.ItemTaskBinding
import com.example.workhive.databinding.ItemTaskDeadlineBinding
import com.example.workhive.model.Task
import com.example.workhive.model.UpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoAdapter(
    private val tasks: MutableList<Task>,
    private val onStatusUpdated: () -> Unit,
    private val onStatusUpdatedWithData: (Task) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    inner class ToDoViewHolder(val binding: ItemTaskDeadlineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = ItemTaskDeadlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val task = tasks[position]
        val binding = holder.binding


            binding.taskTitle.text = task.title
            binding.taskDueDate.text =task.due_date
            binding.taskStatus.text = task.status
            updateStatusColor(task.status, binding)
            updateButtonUI(task.status, binding)
            binding.btnUpdateStatus.setOnClickListener {
                val newStatus = when (task.status) {
                    "Pending" -> "Doing"
                    "Doing" -> "Done"
                    else -> task.status // Không thay đổi nếu đã "Done"
                }

                if (task.status != "Done") {
                    updateTaskStatus(holder.itemView.context, task, position, newStatus,binding )
                }
            }
    }

    private fun updateStatusColor(status: String, binding: ItemTaskDeadlineBinding) {
        val color = when (status) {
            "Pending" -> R.color.pending
            "Doing" -> R.color.doing
            "Done" -> R.color.done
            "Over Due" -> R.color.over
            else -> R.color.black // Mặc định nếu không khớp
        }
        binding.taskStatus.setTextColor(ContextCompat.getColor(binding.root.context, color))
    }


    private fun updateButtonUI(status: String, binding: ItemTaskDeadlineBinding) {
        when (status) {
            "Pending" -> {
                binding.btnUpdateStatus.text = "Doing"
                binding.btnUpdateStatus.isEnabled = true
            }
            "Doing" -> {

                binding.btnUpdateStatus.text = "Done"
                binding.btnUpdateStatus.isEnabled = true
            }
            "Done" -> {

                binding.btnUpdateStatus.text = "Completed"
                binding.btnUpdateStatus.isEnabled = false
            }
            else ->{

                binding.btnUpdateStatus.text = "Completed"
                binding.btnUpdateStatus.isEnabled = false
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    private fun updateTaskStatus(
        context: Context,
        task: Task,
        position: Int,
        newStatus: String,
        binding: ItemTaskDeadlineBinding
    ) {
        val request = UpdateRequest(task.task_id, newStatus)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitTask.taskApi.updateStatus(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    task.status = newStatus
                    notifyItemChanged(position)
                    onStatusUpdated()
                    onStatusUpdatedWithData(task)
                    Toast.makeText(context, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show()

                    updateButtonUI(task.status, binding)
                    // Cập nhật hiển thị trạng thái task
                    binding.taskStatus.text = task.status
                } else {
                    Toast.makeText(context, "Thất bại: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
