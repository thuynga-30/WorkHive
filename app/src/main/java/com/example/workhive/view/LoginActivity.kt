package com.example.workhive.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.workhive.databinding.ActivityLoginBinding
import com.example.workhive.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOk.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPass.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            loginViewModel.loginUser(email, password)
        }

        binding.btnDk.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        loginViewModel.loginResult.observe(this, Observer { response ->
            if (response.success) {
                val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                sharedPref.edit().putString("USER_NAME", response.user?.user_name).apply()
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, response.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        })

        loginViewModel.errorMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
    }
}
