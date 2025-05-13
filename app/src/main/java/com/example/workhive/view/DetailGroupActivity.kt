package com.example.workhive.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R
import com.example.workhive.adapter.AddMemberDialog
import com.example.workhive.adapter.CreateTaskDialog
import com.example.workhive.adapter.DetailAdapter
import com.example.workhive.adapter.TaskAdapter
import com.example.workhive.adapter.UpdateGroupDialog
import com.example.workhive.api.RetrofitTask
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.TeamsDetailBinding
import com.example.workhive.helper.BottomNavHelper
import com.example.workhive.model.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailGroupActivity: AppCompatActivity() {
    private lateinit var binding: TeamsDetailBinding
    private lateinit var adapter: DetailAdapter
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var group: Group
    private var members: MutableList<Members> = mutableListOf()  // <- sửa thành mutableList
    private var tasks = mutableListOf<Task>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= TeamsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        BottomNavHelper.setupBottom(this,R.id.menu_home )
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val groupId = intent.getIntExtra("GROUP_ID", -1)
        if (groupId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhóm", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
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

        if (userName == createdBy) {
            binding.removeButton.text = "Remove member"
            binding.removeButton.setOnClickListener {
                showInputMemberToDeleteDialog()
            }
            binding.newTaskButton.setOnClickListener{
                showAddTask(groupId)
            }
            binding.addMemberButton.setOnClickListener {
                showAddMember(group)
            }
            binding.updateButton.setOnClickListener {
                showUpdateGroup(group)
            }
        } else {
            binding.removeButton.text = "Leave group"
            binding.removeButton.setOnClickListener {
                confirmLeaveGroup(userName)
            }
            binding.updateButton.visibility = View.GONE
            binding.addMemberButton.visibility = View.GONE
            binding.newTaskButton.visibility = View.GONE
        }

        loadGroupMembers(groupId)
        taskAdapter = TaskAdapter(group,
            tasks,
            onGroupClicked = { task ->
//
            },
            onGroupDelete ={ task ->
                deleteTask(task)
            })

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = taskAdapter
        loadTask(groupId)
    }

    private fun deleteTask(task: Task) {
        val index = tasks.indexOfFirst { it.group_id == task.group_id }
        if (index != -1) {
            tasks.removeAt(index)
            taskAdapter.notifyItemRemoved(index)
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showAddTask(groupId: Int) {
        val dialog = CreateTaskDialog(groupId){
            loadTask(groupId)
        }
        dialog.show(supportFragmentManager, "CreateGroupDialog")
    }

    private fun loadTask(groupId: Int) {
        RetrofitTask.taskApi.getTask(groupId).enqueue(object: Callback<GetTasksResponse>{
            override fun onResponse(call: Call<GetTasksResponse>, response: Response<GetTasksResponse>){
                if (response.isSuccessful) {
                    val tasksResponse = response.body()
                    if (tasksResponse != null && tasksResponse.success) {
                        tasks.clear()
                        tasksResponse.tasks?.forEach { task ->
                            if (isOverdue(task.due_date.toString()) && task.status != "Done") {
                                task.status = "Over Due" // Đặt trạng thái thành "Over"
                            }
                        }
                        tasksResponse.tasks?.let { tasks.addAll(it) }
                        taskAdapter.notifyDataSetChanged()

                    } else {
                        Log.e("API_ERROR", "Response body: ${response.errorBody()?.string()}")
                    }
                } else {
                    Log.e("DEBUG_API_TASK", "Error body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@DetailGroupActivity, "Server error", Toast.LENGTH_SHORT).show()
                }
            }

            private fun isOverdue(dueDate: String): Boolean {
                return try {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val taskDate = format.parse(dueDate)
                    val currentDate = Date()

                    taskDate != null && taskDate.before(currentDate) // Quá hạn nếu trước ngày hiện tại
                } catch (e: Exception) {
                    false // Nếu lỗi định dạng, mặc định là không quá hạn
                }
            }

            override fun onFailure(call: Call<GetTasksResponse>, t: Throwable) {
                Log.e("DEBUG_API_TASK", "Connection failed: ${t.message}")
                Toast.makeText(this@DetailGroupActivity, "Connection failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun showUpdateGroup(group: Group) {
        val dialog = UpdateGroupDialog(
            group.group_id,
            currentName = group.name,
            currentDesc = group.description,
            onSubmit = { groupName,groupDescription ->
                group.name = groupName
                group.description =groupDescription
                loadGroupDetail(group.group_id)
            }
        )
        dialog.show(
            (binding.root.context as AppCompatActivity).supportFragmentManager,
            "UpdateGroupDialog"
        )
    }

    private fun loadGroupDetail(groupId: Int) {

        RetrofitTeam.teamApi.getGroupById(1,groupId).enqueue(object : Callback<GroupResponse> {
            override fun onResponse(call: Call<GroupResponse>, response: Response<GroupResponse>) {
                Log.d("DEBUG_API", "Response code: ${response.code()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val groupData = response.body()?.group
                    if (groupData != null) {
                        // Cập nhật lại biến group
                        group = Group(
                            groupData.group_id,
                            groupData.name,
                            groupData.description,
                            groupData.created_by,
                            arrayListOf() // không cần cập nhật members ở đây
                        )
                        binding.teamName.text = group.name
                        binding.teamDescription.text = group.description

                        Toast.makeText(this@DetailGroupActivity, "Cập nhật nhóm thành công", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("DEBUG_API", "API lỗi: ${response.errorBody()?.string()}")
                    Toast.makeText(this@DetailGroupActivity, "Không tải được thông tin nhóm", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GroupResponse>, t: Throwable) {
                Log.e("DEBUG_API", "Lỗi gọi API: ${t.message}")
                Toast.makeText(this@DetailGroupActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmLeaveGroup(userName: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm leaving the group")
            .setMessage("Are you sure you want to leave the group?")
            .setPositiveButton("Leave") { _, _ ->
                leaveGroup(userName)
            }
            .setNegativeButton("Cancle", null)
            .show()
    }

    private fun leaveGroup(userName: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitTeam.teamApi.removeMember(
                    userName,
                    RemoveUserRequest(group.group_id, userName)
                )
                if (response.success) {
                    val resultIntent = Intent().apply {
                        putExtra("REMOVED_GROUP_ID", group.group_id)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@DetailGroupActivity, response.message ?: "Thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailGroupActivity, "Lỗi mạng: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadGroupMembers(groupId: Int) {

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
            .setTitle("Remove member from group")
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                val memberName = inputEditText.text.toString().trim()
                if (memberName.isNotEmpty()) {
                    deleteMember(memberName)

                } else {
                    Toast.makeText(this, "Please enter member name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancle", null)
            .show()
    }

    private fun deleteMember(memberToRemove: String) {
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val request = RemoveUserRequest(group.group_id, memberToRemove)
        lifecycleScope.launch {
            try {
                val response = RetrofitTeam.teamApi.removeMember(userName, request)
                if (response.success) {
                    Toast.makeText(
                        this@DetailGroupActivity,
                        "Đã xóa $memberToRemove ra khỏi nhóm",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadGroupMembers(group.group_id) // <-- hoặc reload UI ở đây
                } else {
                    Toast.makeText(
                        this@DetailGroupActivity,
                        response.message ?: "Thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DetailGroupActivity,
                    "Lỗi mạng: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

