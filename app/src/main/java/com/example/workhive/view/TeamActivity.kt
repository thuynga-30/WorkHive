package com.example.workhive.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.workhive.R
import com.example.workhive.adapter.TeamCardAdapter
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.ActivityTeamsBinding
import com.example.workhive.model.*
import retrofit2.*

class TeamActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTeamsBinding
    private lateinit var adapter: TeamCardAdapter
    private val teamList = mutableListOf<Group>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TeamCardAdapter(teamList,
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

        RetrofitTeam.teamApi.getGroups(userName).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        teamList.clear()
                        apiResponse.created_groups?.let { teamList.addAll(it) }
                        apiResponse.joined_groups?.let { teamList.addAll(it) }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@TeamActivity, apiResponse?.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this@TeamActivity,
                        "Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@TeamActivity, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}