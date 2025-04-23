package com.example.workhive.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.workhive.databinding.ActivityRegisterBinding
import com.example.workhive.model.Users
import com.example.workhive.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val user_name = binding.editUserName.text.toString().trim()
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPass.text.toString().trim()

            if (user_name.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter necessary information", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val user = Users(user_name, name, email, password)
            registerViewModel.registerUser(user)
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        registerViewModel.registerResult.observe(this, Observer { response ->
            if (response.success) {
                Toast.makeText(this, "Register successful!!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, response.message ?: "Register failed", Toast.LENGTH_SHORT).show()
            }
        })

        registerViewModel.errorMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
    }
}
