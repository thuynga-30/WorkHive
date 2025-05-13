package com.example.workhive.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R
import com.example.workhive.adapter.EvaluationAdapter
import com.example.workhive.api.RetrofitEvaluate
import com.example.workhive.databinding.ActivityEvaluationBinding
import com.example.workhive.helper.BottomNavHelper
import com.example.workhive.model.Evaluation
import com.example.workhive.model.EvaluationResponse
import com.example.workhive.model.GeneralResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailEvaluationActivity:AppCompatActivity() {
    private lateinit var binding: ActivityEvaluationBinding
    private lateinit var adapter: EvaluationAdapter
    private var evaluations = mutableListOf<Evaluation>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvaluationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        BottomNavHelper.setupBottom(this, R.id.menu_home )
        val groupId = intent.getIntExtra("GROUP_ID", -1)
        if (groupId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhóm", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        adapter = EvaluationAdapter(evaluations)
        binding.rvEvaluations.layoutManager = LinearLayoutManager(this)
        binding.rvEvaluations.adapter = adapter
        createEvaluation(groupId)
    }

    private fun loadData(groupId: Int) {
        RetrofitEvaluate.api.getEvaluations(groupId).enqueue(object : Callback<EvaluationResponse>{
            override fun onResponse(
                call: Call<EvaluationResponse>,
                response: Response<EvaluationResponse>
            ) {
                if (response.isSuccessful) {
                    val evaluationResponse = response.body()
                    if (evaluationResponse != null && evaluationResponse.success) {
                        evaluations.clear()
                        evaluationResponse.evaluations?.let {
                            Log.d("DEBUG_DATA", "Loaded ${it.size} evaluations")
                            for (e in it) {
                                Log.d("DEBUG_DATA_ITEM", "${e.evaluated_user} - ${e.total_subtasks}")
                            }
                            evaluations.addAll(it)
                            adapter.notifyDataSetChanged()

                        }

                    }
                }
            }

            override fun onFailure(call: Call<EvaluationResponse>, t: Throwable) {
                Log.e("DEBUG_API_TASK", "Connection failed: ${t.message}")
                Toast.makeText(this@DetailEvaluationActivity, "Connection failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun createEvaluation(groupId: Int) {
        RetrofitEvaluate.api.createEvaluations(groupId).enqueue(object : Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    loadData(groupId)
                } else {
                    Toast.makeText(this@DetailEvaluationActivity, "Thất bại: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Log.e("DEBUG_API_TASK", "Connection failed: ${t.message}")
                Toast.makeText(this@DetailEvaluationActivity, "Connection failed: ${t.message}", Toast.LENGTH_SHORT).show()            }
        })
    }

}
