package com.example.workhive.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workhive.R
import com.example.workhive.adapter.NotifyAdapter
import com.example.workhive.api.RetrofitNotify

import com.example.workhive.databinding.ActivityNotifyBinding
import com.example.workhive.helper.BottomNavHelper

import com.example.workhive.model.Notification
import kotlinx.coroutines.launch


class NotifyActivity: AppCompatActivity() {
    private lateinit var binding: ActivityNotifyBinding
    private lateinit var notifyAdapter : NotifyAdapter
    private val notify = mutableListOf<Notification>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityNotifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        BottomNavHelper.setupBottom(this, R.id.menu_home)
        binding.notifyButton.setOnClickListener {
            markAsRead()
        }
        binding.btnRemove.setOnClickListener {
            showConfirmDelete()
        }
        notifyAdapter = NotifyAdapter(notify)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = notifyAdapter
        loadData()
    }

    private fun showConfirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa tất cả thông báo")
            .setMessage("Bạn có chắc chắn muốn xóa tất cả thông báo không?")
            .setPositiveButton("Xác nhận") { _, _ ->
                deleteNotify()
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun deleteNotify() {
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""

        // Kiểm tra nếu userName trống
        if (userName.isEmpty()) {
            Toast.makeText(this, "Không có userName hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitNotify.notifyApi.deleteAll(userName)
                if (response.isSuccessful){
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(this@NotifyActivity, "Đã xóa tất cả thông báo", Toast.LENGTH_SHORT).show()
                        loadData()
                    } else {
                        Log.e("Notify", "Lỗi body: $body")
                        Toast.makeText(this@NotifyActivity, "Thất bại: ${body?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("Notify", "Lỗi HTTP: $error")
                    Toast.makeText(this@NotifyActivity, "Lỗi server: $error", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@NotifyActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun markAsRead() {
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""

        // Kiểm tra nếu userName trống
        if (userName.isEmpty()) {
            Toast.makeText(this, "Không có userName hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Gửi yêu cầu với Retrofit
                val response = RetrofitNotify.notifyApi.markAllAsRead(userName)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(this@NotifyActivity, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
                        loadData()
                    } else {
                        Log.e("Notify", "Lỗi body: $body")
                        Toast.makeText(this@NotifyActivity, "Thất bại: ${body?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("Notify", "Lỗi HTTP: $error")
                    Toast.makeText(this@NotifyActivity, "Lỗi server: $error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {

                Toast.makeText(this@NotifyActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadData() {
        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitNotify.notifyApi.getNotify(userName)
                if (response.isSuccessful && response.body()?.success == true) {
                    val notifications = response.body()?.notify ?: emptyList()
                    notify.clear()
                    notify.addAll(notifications)
                    notifyAdapter.notifyDataSetChanged()
                    Log.d("SUCCESS","Đã lấy thành công")
                } else {
                    Log.d("ERROR","Lỗi tải thông báo")
                    Toast.makeText(this@NotifyActivity, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("DEBUG HOME","Lỗi kết nối: ${e.message}")
                Toast.makeText(this@NotifyActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
