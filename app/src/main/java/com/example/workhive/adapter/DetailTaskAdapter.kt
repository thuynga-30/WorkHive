package com.example.workhive.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workhive.databinding.ItemSubtaskBinding
import com.example.workhive.model.Task

class DetailTaskAdapter(private val taskList: List<Task>) :
    RecyclerView.Adapter<DetailTaskAdapter.TaskViewHolder>() {
    inner class TaskViewHolder(private val binding: ItemSubtaskBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind (task: Task){
            binding.apply {
                textSubtaskTitle.text = task.title
                textAssignedTo.text = "Asigned to: ${task.assigned_to}"
                taskDueDate.text = task.due_date
                taskStatus.text = task.status
                taskStatus.setTextColor(
                    when (task.status) {
                        "Pending" -> Color.BLACK
                        "Doing" -> Color.BLUE
                        "Done" -> Color.GREEN
                        else -> Color.RED
                    }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailTaskAdapter.TaskViewHolder {
        val binding = ItemSubtaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)

    }

    override fun onBindViewHolder(holder: DetailTaskAdapter.TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = taskList.size
}
