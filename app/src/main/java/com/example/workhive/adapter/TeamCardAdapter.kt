package com.example.workhive.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.workhive.api.RetrofitTeam
import com.example.workhive.databinding.ItemTeamCardBinding
import com.example.workhive.model.*
import com.example.workhive.view.DetailEvaluationActivity
import com.example.workhive.view.DetailGroupActivity
import com.example.workhive.view.TeamActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeamCardAdapter(
    private val teams: MutableList<Group>,
    private val onGroupClicked: (Group) -> Unit,
    private val onGroupDelete: (Group) -> Unit,
    private val isFromLeaderPage: Boolean
) : RecyclerView.Adapter<TeamCardAdapter.TeamViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding= ItemTeamCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teams[position]
        holder.bind(team)
    }

    override fun getItemCount(): Int = teams.size

    inner class TeamViewHolder(private val binding: ItemTeamCardBinding):
    RecyclerView.ViewHolder(binding.root) {
        fun bind(team: Group) {
            binding.apply {
                teamName.text = team.name
                teamDescription.text = team.description
                val sharedPref = binding.root.context.getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                val userName = sharedPref.getString("USER_NAME", "") ?: ""
                removeButton.visibility = if (isFromLeaderPage && team.created_by == userName) View.VISIBLE else View.GONE
                removeButton.setOnClickListener {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete this group?")
                        .setPositiveButton("Delete") { _, _ ->
                            requestDeleteGroup(team)
                        }
                        .setNegativeButton("Cancle", null)
                        .show()
                }
                val activity = itemView.context as? Activity
                if (activity is TeamActivity) {
                    detailButton.setOnClickListener {
                        showDetailGroup(team)
                    }
                } else {
                    detailButton.text ="View Rating"
                    detailButton.setOnClickListener {
                        showEvaluation(team)
                    }
                }
                root.setOnClickListener {
                    onGroupClicked(team) // <- Gọi callback khi click vào item
                }
            }
        }

        private fun showEvaluation(team: Group) {
            val intent = Intent(itemView.context, DetailEvaluationActivity::class.java).apply {
                putExtra("GROUP_ID", team.group_id)
            }
            itemView.context.startActivity(intent)
        }


        private fun showDetailGroup(team: Group) {
            val intent = Intent(itemView.context, DetailGroupActivity::class.java).apply {
                putExtra("GROUP_ID", team.group_id)
                putExtra("GROUP_NAME", team.name)
                putExtra("GROUP_DESCRIPTION", team.description)
                putStringArrayListExtra("GROUP_MEMBERS", ArrayList(team.members ?: listOf()))
                putExtra("GROUP_CREATED_BY", team.created_by)

            }
            itemView.context.startActivity(intent)
        }
        private fun requestDeleteGroup(team: Group) {
            val context = binding.root.context
            val sharedPref = context.getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            val userName = sharedPref.getString("USER_NAME", "") ?: ""
            if (userName.isEmpty()) {
                Toast.makeText(context, "User not logged in. Please log in.", Toast.LENGTH_SHORT).show()
                return
            }

            val request = DeleteGroupRequest(team.group_id)

            RetrofitTeam.teamApi.deleteGroup(userName,request).enqueue(object :
                Callback<DeleteResponse> {
                override fun onResponse(
                    call: Call<DeleteResponse>,
                    response: Response<DeleteResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.success == true) {
                            Toast.makeText(context, "Delete Group successfully", Toast.LENGTH_SHORT)
                                .show()
                            onGroupDelete(team)
                        } else {
                            Log.e("DeleteGroup", "Failed: ${responseBody?.message}")
                            Toast.makeText(
                                context,
                                responseBody?.message ?: "Failed to delete",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e(
                            "DeleteGroup",
                            "Server Error: ${response.code()} - ${response.message()}"
                        )
                        Toast.makeText(
                            context,
                            "Server Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    Log.e("DeleteGroup", "Network Error: ${t.message}")
                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Failed to connect to server. Please check your network connection.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            })
        }
    }
}