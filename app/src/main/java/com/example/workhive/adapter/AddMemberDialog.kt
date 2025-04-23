package com.example.workhive.adapter

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.AddMembersBinding
import com.example.workhive.model.AddMemberRequest
import com.example.workhive.model.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMemberDialog(private val groupId: Int, private val onMemberAdded: (String) -> Unit) : DialogFragment() {

    private var _binding: AddMembersBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddMembersBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnAdd.setOnClickListener {
            val memberName = binding.inputMemberName.text.toString()
            if (memberName.isNotEmpty()) {
                addMemberToGroup(groupId, memberName)
            } else {
                Toast.makeText(context, "Please enter a member name", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

    }

    private fun addMemberToGroup(groupId: Int, memberName: String) {
        val sharedPref = requireContext().getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val request = AddMemberRequest(groupId, memberName)

        RetrofitTeam.teamApi.addMember(userName,request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "$memberName added successfully", Toast.LENGTH_SHORT).show()
                    onMemberAdded(memberName)  // callback báo về Activity/Fragment
                    dismiss()                  // đóng dialog
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Failed to add member", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}