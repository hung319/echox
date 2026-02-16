package com.telegram.videoplayer.domain.repository

import com.telegram.videoplayer.data.remote.telegram.AuthState
import com.telegram.videoplayer.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun sendCode(phoneNumber: String): Result<Unit>
    suspend fun checkCode(code: String): Result<Unit>
    suspend fun getCurrentUser(): Result<User>
    fun observeAuthState(): Flow<AuthState>
    suspend fun logout()
    fun isAuthenticated(): Boolean
}
