package com.example.workhive.adapter

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.workhive.api.RetrofitTask
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.CreateSubtaskBinding
import com.example.workhive.model.CreateSubTaskRequest
import com.example.workhive.model.MemberResponse
import com.example.workhive.model.getResponse
import retrofit2.*
import java.util.Calendar

class AssignToDialog(private val taskId: Int, private val groupId: Int, private val onTaskAdded: () -> Unit) : DialogFragment() {
    private var _binding : CreateSubtaskBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CreateSubtaskBinding.inflate(inflater,container,false)
        return binding.root
    }
//    private var memberNames: List<String> = emptyList()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        // Lấy thành viên 1 lần
        RetrofitTeam.teamApi.getMembersOfGroup(groupId)
            .enqueue(object : Callback<MemberResponse> {
                override fun onResponse(call: Call<MemberResponse>, response: Response<MemberResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val memberNames = response.body()?.members?.map { it.user_name } ?: emptyList()
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            memberNames
                        )
                        binding.etAssignedTo.setAdapter(adapter)
                    } else {
                        Toast.makeText(requireContext(), "Không lấy được thành viên", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MemberResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        // Hiển thị dropdown khi nhấn
        binding.etAssignedTo.setOnClickListener {
            binding.etAssignedTo.showDropDown()
        }
        binding.btnCreateTask.setOnClickListener {
            val title = binding.editTaskTitle.text.toString().trim()
            val description = binding.editTaskDescription.text.toString().trim()
            val assignedTo = binding.etAssignedTo.text.toString()
            val dueDate = binding.editTextDate.text.toString()
            if (title.isEmpty() || assignedTo.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                createSubTask(taskId,title,description,assignedTo,groupId,dueDate)
            }
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun createSubTask(
        taskId: Int,
        title: String,
        description: String,
        assignedTo: String,
        groupId: Int,
        dueDate: String
    ) {
        val request = CreateSubTaskRequest(taskId,title,description,assignedTo,groupId,dueDate)
        RetrofitTask.taskApi.createSubTask(request).enqueue(object : Callback<getResponse>{
            override fun onResponse(call: Call<getResponse>, response: Response<getResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Tạo subtask thành công", Toast.LENGTH_SHORT).show()
                    onTaskAdded()  // callback báo về Activity/Fragment
                    dismiss()                  // đóng dialog
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Tạo thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<getResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
