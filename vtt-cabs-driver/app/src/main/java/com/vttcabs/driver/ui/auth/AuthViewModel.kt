package com.vttcabs.driver.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val email: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // In production, call Supabase auth API
                // For demo, we'll just set success
                kotlinx.coroutines.delay(1000)
                _state.value = _state.value.copy(
                    isLoading = false,
                    email = email,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send OTP"
                )
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // In production, verify OTP with Supabase
                // Demo: Accept '123456' as valid OTP
                kotlinx.coroutines.delay(1000)
                
                if (otp == "123456") {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Invalid OTP"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Verification failed"
                )
            }
        }
    }

    fun logout() {
        _state.value = AuthState()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
