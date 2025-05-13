package com.example.workhive.adapter

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.workhive.api.RetrofitTask
import com.example.workhive.databinding.CreateTeamBinding
import com.example.workhive.databinding.DialogCreateTaskBinding
import com.example.workhive.model.CreateTaskRequest
import com.example.workhive.model.UpdateTaskRequest
import com.example.workhive.model.getResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class UpdateTaskDialog(
    private val taskId: Int,
    private val currentName: String,
    private val currentDesc: String?,
    private val currentDate: String?,
    private val onUpdate: (String,String,String) -> Unit
    ): DialogFragment() {
    private var _binding: DialogCreateTaskBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dialogTitle.text ="Update Tasks"
        binding.editTaskTitle.setText(currentName)
        binding.editTaskDescription.setText(currentDesc)
        binding.editTextDate.setText(currentDate)
        binding.btnCreateTask.text="Save"
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
                    Log.d("CreateTaskDialog", "Selected date: $date")
                    binding.editTextDate.setText(date)
                }, year, month, day
            )
            datePickerDialog.show()
        }

        binding.btnCreateTask.setOnClickListener {
            val title = binding.editTaskTitle.text.toString()
            val description = binding.editTaskDescription.text.toString()
            val date = binding.editTextDate.text.toString()
            Log.d("CreateTaskDialog", "Date input: $date")
            if (title.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty()) {
                updateTask(taskId, title, description, date)
            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateTask(taskId: Int, title: String, description: String, date: String) {
        val request = UpdateTaskRequest(taskId,title, description, date)
        RetrofitTask.taskApi.updateTask(request).enqueue(object : Callback<getResponse> {
            override fun onResponse(call: Call<getResponse>, response: Response<getResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(context, "Group updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        onUpdate(title
                            ,description, date)
                        dismiss()  // Close the dialog
                    } else {
                        Toast.makeText(context, "lỗi", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<getResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}