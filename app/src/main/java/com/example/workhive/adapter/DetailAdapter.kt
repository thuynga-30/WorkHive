package com.example.workhive.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workhive.databinding.ItemTeamMemberBinding
import com.example.workhive.model.Members

class DetailAdapter(
    private var members: MutableList<Members>
): RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(private val binding: ItemTeamMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: Members) {
            binding.apply {
                tvUserName.text = member.user_name
                tvRole.text = "${member.role}"
            }
        }

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailAdapter.DetailViewHolder {
        val binding = ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailAdapter.DetailViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member)
    }

    override fun getItemCount(): Int = members.size

}

