package com.telegram.videoplayer.data.repository

import com.telegram.videoplayer.data.remote.telegram.AuthState
import com.telegram.videoplayer.data.remote.telegram.TelegramClient
import com.telegram.videoplayer.data.remote.telegram.TelegramMapper
import com.telegram.videoplayer.domain.model.User
import com.telegram.videoplayer.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val telegramClient: TelegramClient
) : AuthRepository {
    
    override suspend fun sendCode(phoneNumber: String): Result<Unit> {
        return try {
            telegramClient.sendCode(phoneNumber)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun checkCode(code: String): Result<Unit> {
        return try {
            telegramClient.checkCode(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val tdUser = telegramClient.getMe()
            Result.success(TelegramMapper.mapUserToDomain(tdUser))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeAuthState(): Flow<AuthState> = flow {
        emit(if (telegramClient.isAuthenticated()) AuthState.Authenticated else AuthState.WaitingForPhoneNumber)
    }
    
    override suspend fun logout() {
        telegramClient.logout()
    }
    
    override fun isAuthenticated(): Boolean {
        return telegramClient.isAuthenticated()
    }
}
