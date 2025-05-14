package com.example.workhive.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workhive.model.RegisterResponse
import com.example.workhive.model.Users
import com.example.workhive.repository.AuthRepository
import kotlinx.coroutines.launch


class RegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _registerResult = MutableLiveData<RegisterResponse>()
    val registerResult: LiveData<RegisterResponse> = _registerResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun registerUser(user: Users) {
        viewModelScope.launch {
            try {
                val response = authRepository.register(user)
                if (response.isSuccessful && response.body() != null) {
                    _registerResult.value = response.body()
                } else {
                    _errorMessage.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            }
        }
    }

}
