package com.example.workhive.adapter

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.CreateTeamBinding
import com.example.workhive.model.ApiResponse
import com.example.workhive.model.CreateGroupRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateGroupDialog(private val onGroupCreated: () -> Unit): DialogFragment() {

    private var _binding: CreateTeamBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateTeamBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.createButton.setOnClickListener {
            val groupName = binding.teamNameInput.text.toString().trim()
            val groupDescription = binding.descriptionInput.text.toString().trim()
            if (groupName.isNotEmpty() && groupDescription.isNotEmpty()) {
                createGroup(groupName, groupDescription)
            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createGroup(groupName: String, groupDescription: String) {
        val sharedPref = requireContext().getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val request = CreateGroupRequest(groupName,groupDescription,userName)

        RetrofitTeam.teamApi.createGroup(userName,request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
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

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


