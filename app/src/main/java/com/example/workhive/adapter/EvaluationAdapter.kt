package com.example.workhive.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workhive.databinding.ItemEvaluationBinding
import com.example.workhive.databinding.ItemTeamMemberBinding
import com.example.workhive.model.Evaluation
import com.example.workhive.model.Members

class EvaluationAdapter  ( private var evaluations: MutableList<Evaluation>
): RecyclerView.Adapter<EvaluationAdapter.EvaluationViewHolder>() {

    inner class EvaluationViewHolder(private val binding: ItemEvaluationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(evaluation:Evaluation ) {
            binding.apply {
                tvUser.text = evaluation.evaluated_user
                tvCompleted.text = evaluation.ontime_subtasks.toString()
                tvTotal.text = evaluation.total_subtasks.toString()
                tvRating.text = evaluation.rating
                tvRating.setTextColor(
                    when(evaluation.rating){
                        "Good"-> Color.GREEN
                        "Quite Good"-> Color.BLUE
                        else -> Color.RED
                    }
                )
            }
        }

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EvaluationAdapter.EvaluationViewHolder {
        val binding = ItemEvaluationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return EvaluationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EvaluationAdapter.EvaluationViewHolder, position: Int) {
        val evaluation = evaluations[position]
        holder.bind(evaluation)
    }

    override fun getItemCount(): Int = evaluations.size

}