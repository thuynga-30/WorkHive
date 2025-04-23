package com.example.workhive.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R
import com.example.workhive.adapter.AddMemberDialog
import com.example.workhive.adapter.DetailAdapter
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.TeamsDetailBinding
import com.example.workhive.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailGroupActivity: AppCompatActivity() {
    private lateinit var binding: TeamsDetailBinding
    private lateinit var adapter: DetailAdapter
    private lateinit var group: Group
    private var members: MutableList<Members> = mutableListOf()  // <- sửa thành mutableList
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= TeamsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val groupId = intent.getIntExtra("GROUP_ID", -1)
        val groupName = intent.getStringExtra("GROUP_NAME") ?: "Unknown"
        val groupDescription = intent.getStringExtra("GROUP_DESCRIPTION") ?: ""
        val groupMembers = intent.getStringArrayListExtra("GROUP_MEMBERS") ?: arrayListOf()
        val createdBy = intent.getStringExtra("GROUP_CREATED_BY")?:""
        group = Group(groupId, groupName, groupDescription,createdBy ,groupMembers)
        binding.teamName.text = groupName
        binding.teamDescription.text = groupDescription
        adapter = DetailAdapter(members)
        binding.membersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.membersRecyclerView.adapter = adapter
        binding.addMemberButton.setOnClickListener {
            showAddMember(group)
        }
        if (userName == createdBy) {
            binding.removeButton.text = "Xoá thành viên"
            binding.removeButton.setOnClickListener {
                showInputMemberToDeleteDialog()
            }
        } else {
            binding.removeButton.text = "Rời nhóm"
            binding.removeButton.setOnClickListener {
                confirmLeaveGroup(userName)
            }
        }
        loadGroupMembers(groupId)

    }
    private fun confirmLeaveGroup(userName: String) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận rời nhóm")
            .setMessage("Bạn có chắc chắn muốn rời khỏi nhóm không?")
            .setPositiveButton("Rời nhóm") { _, _ ->
                leaveGroup(userName)
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun leaveGroup(userName: String) {
        val request = RemoveUserRequest(group.group_id, userName)
        RetrofitTeam.teamApi.removeMember(userName, request).enqueue(object :
            Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(
                        this@DetailGroupActivity,
                        "Bạn đã rời khỏi nhóm",
                        Toast.LENGTH_SHORT
                    ).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra("REMOVED_GROUP_ID", group.group_id)
                    setResult(RESULT_OK, resultIntent)
                    finish()  // hoặc quay về danh sách nhóm
                } else {
                    Toast.makeText(
                        this@DetailGroupActivity,
                        response.body()?.message ?: "Thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                Toast.makeText(this@DetailGroupActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadGroupMembers(groupId: Int) {

        Log.d("DEBUG_API", "Gọi API lấy thành viên với group_id=$groupId")

        RetrofitTeam.teamApi.getMembersOfGroup(groupId).enqueue(object : Callback<MemberResponse> {
            override fun onResponse(call: Call<MemberResponse>, response: Response<MemberResponse>) {
                Log.d("DEBUG_API", "Response code: ${response.code()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val memberList = response.body()?.members ?: emptyList()
                    Log.d("DEBUG_API", "Danh sách thành viên: $memberList")
                    members.clear()
                    members.addAll(memberList)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("DEBUG_API", "API trả về lỗi: ${response.errorBody()?.string()}")
                    Toast.makeText(this@DetailGroupActivity, "Không tải được thành viên", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MemberResponse>, t: Throwable) {
                Log.e("DEBUG_API", "Lỗi gọi API: ${t.message}")
                Toast.makeText(this@DetailGroupActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddMember(group: Group) {
        val dialog = AddMemberDialog(group.group_id) { memberName ->
            group.members.add(memberName)
            loadGroupMembers(group.group_id)
        }
        dialog.show(
            (binding.root.context as AppCompatActivity).supportFragmentManager,
            "AddMemberDialog"
        )
    }
    private fun showInputMemberToDeleteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_member, null)
        val inputEditText = dialogView.findViewById<android.widget.EditText>(R.id.inputMemberName)
        AlertDialog.Builder(this)
            .setTitle("Xoá thành viên khỏi nhóm")
            .setView(dialogView)
            .setPositiveButton("Xoá") { _, _ ->
                val memberName = inputEditText.text.toString().trim()
                if (memberName.isNotEmpty()) {
                    deleteMember(memberName)

                } else {
                    Toast.makeText(this, "Vui lòng nhập tên thành viên", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun deleteMember(memberToRemove: String) {
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val request = RemoveUserRequest(group.group_id, memberToRemove)

        RetrofitTeam.teamApi.removeMember(userName, request).enqueue(object :
            Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@DetailGroupActivity, "Đã xóa $memberToRemove ra khỏi nhóm", Toast.LENGTH_SHORT).show()
                    loadGroupMembers(group.group_id)
                } else {
                    Toast.makeText(this@DetailGroupActivity, response.body()?.message ?: "Thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                Toast.makeText(this@DetailGroupActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

