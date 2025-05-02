package com.example.workhive.adapter

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.workhive.R
import com.example.workhive.api.RetrofitTask
import com.example.workhive.databinding.DialogCreateTaskBinding
import com.example.workhive.databinding.DialogCreateTeamBinding
import com.example.workhive.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class CreateTaskDialog(private val groupId: Int,private val onGroupCreated: () -> Unit): DialogFragment() {
    private var _binding : DialogCreateTaskBinding? =null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCreateTaskBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),
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
                createTask(groupId, title, description, date)
            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun createTask(groupId: Int,title: String, description: String, date: String) {
        val request = CreateTaskRequest(title,description,groupId,date)
        RetrofitTask.taskApi.createTask(request).enqueue(object : Callback<getResponse> {
            override fun onResponse(call: Call<getResponse>, response: Response<getResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(context, "Group created successfully", Toast.LENGTH_SHORT).show()
                        onGroupCreated.invoke()
                        dismiss()  // Close the dialog
                    } else {
                        Toast.makeText(context, "lỗi", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<getResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
