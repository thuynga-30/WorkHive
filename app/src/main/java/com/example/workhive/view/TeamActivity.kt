package com.example.workhive.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R

import com.example.workhive.adapter.CreateGroupDialog
import com.example.workhive.adapter.TeamCardAdapter
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.ActivityTeamsBinding
import com.example.workhive.helper.BottomNavHelper
import com.example.workhive.model.*
import kotlinx.coroutines.launch
import retrofit2.*

class TeamActivity: AppCompatActivity() {
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val removedGroupId = result.data?.getIntExtra("REMOVED_GROUP_ID", -1)
            if (removedGroupId != null && removedGroupId != -1) {
                val iterator = teamList.iterator()
                while (iterator.hasNext()) {
                    val group = iterator.next()
                    if (group.group_id == removedGroupId) {
                        iterator.remove()
                        break
                    }
                }
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Bạn đã rời khỏi nhóm", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private lateinit var binding: ActivityTeamsBinding
    private lateinit var adapter: TeamCardAdapter
    private val teamList = mutableListOf<Group>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        BottomNavHelper.setupBottom(this,R.id.menu_home )
        adapter = TeamCardAdapter(teamList,
            onGroupClicked = { group ->
                val intent = Intent(this, DetailGroupActivity::class.java).apply {
                    putExtra("GROUP_ID", group.group_id)
                    putExtra("GROUP_NAME", group.name)
                    putExtra("GROUP_DESCRIPTION", group.description)
                    putStringArrayListExtra("GROUP_MEMBERS", ArrayList(group.members))
                    putExtra("GROUP_CREATED_BY", group.created_by)
                }
                detailLauncher.launch(intent) // dùng launcher!
            },
            onGroupDelete = { group ->
            deleteGroup(group)
        })
        binding.teamsRecyclerView.adapter = adapter
        binding.teamsRecyclerView.layoutManager = LinearLayoutManager(this)
        loadSampleData()
        binding.newTeamButton.setOnClickListener{
            showCreateTeamDialog()
        }
    }

    private fun deleteGroup(group: Group) {
        val index = teamList.indexOfFirst { it.group_id == group.group_id }
        if (index != -1) {
            teamList.removeAt(index)
            adapter.notifyItemRemoved(index)
            Toast.makeText(this, "Xoá nhóm thành công", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCreateTeamDialog() {
        val dialog = CreateGroupDialog {
            loadSampleData()
        }
        dialog.show(supportFragmentManager, "CreateGroupDialog")
    }



    private fun loadSampleData() {
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitTeam.teamApi.getGroups(userName)
                if (response.success) {
                    teamList.clear()
                    val created = response.created_groups ?: emptyList()
                    val joined = response.joined_groups ?: emptyList()
                    teamList.addAll(created + joined)
                    adapter.notifyDataSetChanged()

                    Log.d("DEBUG_HOME", "Loaded ${teamList.size} groups")
                } else {
                    Toast.makeText(this@TeamActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("Error","Lỗi tải danh sách nhóm: ${e.localizedMessage}")
                Toast.makeText(
                    this@TeamActivity,
                    "Lỗi tải danh sách nhóm: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSampleData()
    }


}