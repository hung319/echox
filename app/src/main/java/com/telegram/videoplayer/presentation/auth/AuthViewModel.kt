package com.telegram.videoplayer.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegram.videoplayer.domain.repository.AuthRepository
import com.telegram.videoplayer.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _phoneState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val phoneState: StateFlow<UiState<Unit>> = _phoneState.asStateFlow()
    
    private val _codeState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val codeState: StateFlow<UiState<Unit>> = _codeState.asStateFlow()
    
    fun sendCode(phoneNumber: String) {
        viewModelScope.launch {
            _phoneState.value = UiState.Loading
            
            val result = authRepository.sendCode(phoneNumber)
            _phoneState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Failed to send code") }
            )
        }
    }
    
    fun checkCode(code: String) {
        viewModelScope.launch {
            _codeState.value = UiState.Loading
            
            val result = authRepository.checkCode(code)
            _codeState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Invalid code") }
            )
        }
    }
    
    fun resetPhoneState() {
        _phoneState.value = UiState.Idle
    }
    
    fun resetCodeState() {
        _codeState.value = UiState.Idle
    }
}
