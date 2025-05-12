package com.example.workhive.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R
import com.example.workhive.adapter.TeamCardAdapter
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.ActivityTeamsBinding
import com.example.workhive.helper.BottomNavHelper
import com.example.workhive.model.Evaluation
import com.example.workhive.model.Group
import kotlinx.coroutines.launch

class EvaluationActivity:AppCompatActivity() {
    private lateinit var binding: ActivityTeamsBinding
    private lateinit var adapter: TeamCardAdapter
    private val teamList = mutableListOf<Group>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        BottomNavHelper.setupBottom(this, R.id.menu_home )
        binding.teamsHeader.text="Evaluation"
        binding.newTeamButton.visibility = View.GONE
        adapter = TeamCardAdapter(teamList,
            onGroupClicked = {group ->
                val intent = Intent(this, DetailEvaluationActivity::class.java)
                intent.putExtra("GROUP_ID", group.group_id)
                startActivity(intent)
            },
            onGroupDelete = {
            },
            isFromLeaderPage = false)
        binding.teamsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.teamsRecyclerView.adapter =adapter
        loadData()
    }

    private fun loadData() {
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
                    Toast.makeText(this@EvaluationActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("Error","Lỗi tải danh sách nhóm: ${e.localizedMessage}")
                Toast.makeText(
                    this@EvaluationActivity,
                    "Lỗi tải danh sách nhóm: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}