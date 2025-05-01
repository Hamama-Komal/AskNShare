package com.example.asknshare.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ForgotPasswordViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun resetPassword(email: String) {
        if (!email.isValidEmail()) {
            _uiState.value = ForgotPasswordUiState.ValidationError(
                message = "Please enter a valid email"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = ForgotPasswordUiState.Success(email)
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState.Error(
                    message = e.message ?: "Error sending reset email. Please try again."
                )
            }
        }
    }

    fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}



sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class Success(val email: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
    data class ValidationError(val message: String) : ForgotPasswordUiState()
}