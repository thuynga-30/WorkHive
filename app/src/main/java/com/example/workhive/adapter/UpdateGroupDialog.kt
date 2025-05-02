package com.example.workhive.adapter

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.CreateTeamBinding
import com.example.workhive.model.DeleteResponse
import com.example.workhive.model.UpdateGroupRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateGroupDialog(
    private val groupId: Int,
    private val currentName: String,
    private val currentDesc: String,
    private val onSubmit: (String,String) -> Unit
) : DialogFragment() {
    private var _binding: CreateTeamBinding? =null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CreateTeamBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.dialogTitle.text ="Update Teams"
        binding.teamNameInput.setText(currentName)
        binding.descriptionInput.setText(currentDesc)
        binding.createButton.text="Save"
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.createButton.setOnClickListener {
            val groupName = binding.teamNameInput.text.toString().trim()
            val groupDescription = binding.descriptionInput.text.toString().trim()
            updateGroup(groupId,groupName, groupDescription)
        }
    }

    private fun updateGroup(groupId: Int,groupName: String, groupDescription: String) {
        val sharedPref = requireContext().getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val request = UpdateGroupRequest(groupId,groupName,groupDescription)

        RetrofitTeam.teamApi.updateGroup(userName,request).enqueue(object :
            Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(context, "Cập nhật nhóm thành công", Toast.LENGTH_SHORT).show()
                        onSubmit(groupName
                            ,groupDescription)
                        dismiss()  // Close the dialog
                    } else {
                        Log.e("UPDATE_GROUP", "Raw response: ${response.errorBody()?.string()}")
                        Toast.makeText(context, "lỗi", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                Log.e("UPDATE_GROUP", "Failure: ${t.message}")
                Toast.makeText(context, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
