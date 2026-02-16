package com.telegram.videoplayer.data.remote.telegram

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationSupplier
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.jni.TdApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TelegramClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var client: SimpleTelegramClient? = null
    private var isInitialized = false
    
    // TODO: Replace with your actual API credentials from https://my.telegram.org
    private val API_ID = 0 // Replace with actual API ID
    private val API_HASH = "YOUR_API_HASH" // Replace with actual API hash
    
    suspend fun initialize(phoneNumber: String): Flow<AuthState> = callbackFlow {
        try {
            val settings = TDLibSettings.create(APIToken(API_ID, API_HASH))
            settings.databaseDirectoryPath = Paths.get(context.filesDir.absolutePath, "tdlib")
            settings.downloadedFilesDirectoryPath = Paths.get(context.cacheDir.absolutePath, "downloads")
            
            val authSupplier = AuthenticationSupplier { clientId ->
                trySend(AuthState.WaitingForPhoneNumber)
                TdApi.PhoneNumberAuthenticationSettings().apply {
                    allowFlashCall = false
                    isCurrentPhoneNumber = false
                    allowSmsRetrieverApi = false
                }
            }
            
            client = SimpleTelegramClient(settings)
            client?.start(authSupplier)
            isInitialized = true
            
            trySend(AuthState.Authenticated)
        } catch (e: Exception) {
            trySend(AuthState.Error(e.message ?: "Unknown error"))
        }
        
        awaitClose {
            // Keep connection open
        }
    }
    
    suspend fun sendCode(phoneNumber: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { result ->
                when (result) {
                    is TdApi.Ok -> continuation.resume(Result.success(Unit))
                    is TdApi.Error -> continuation.resume(Result.failure(Exception(result.message)))
                    else -> continuation.resume(Result.failure(Exception("Unexpected result")))
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    suspend fun checkCode(code: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            client?.send(TdApi.CheckAuthenticationCode(code)) { result ->
                when (result) {
                    is TdApi.Ok -> continuation.resume(Result.success(Unit))
                    is TdApi.Error -> continuation.resume(Result.failure(Exception(result.message)))
                    else -> continuation.resume(Result.failure(Exception("Unexpected result")))
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    suspend fun getChats(): List<TdApi.Chat> = suspendCancellableCoroutine { continuation ->
        try {
            val chats = mutableListOf<TdApi.Chat>()
            client?.send(TdApi.GetChats(TdApi.ChatListMain(), 100)) { result ->
                when (result) {
                    is TdApi.Chats -> {
                        result.chatIds.forEach { chatId ->
                            client?.send(TdApi.GetChat(chatId)) { chatResult ->
                                if (chatResult is TdApi.Chat) {
                                    chats.add(chatResult)
                                }
                            }
                        }
                        continuation.resume(chats)
                    }
                    is TdApi.Error -> continuation.resumeWithException(Exception(result.message))
                    else -> continuation.resumeWithException(Exception("Unexpected result"))
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    suspend fun getChatHistory(chatId: Long, limit: Int = 100): List<TdApi.Message> = 
        suspendCancellableCoroutine { continuation ->
            try {
                client?.send(TdApi.GetChatHistory(chatId, 0, 0, limit, false)) { result ->
                    when (result) {
                        is TdApi.Messages -> continuation.resume(result.messages.toList())
                        is TdApi.Error -> continuation.resumeWithException(Exception(result.message))
                        else -> continuation.resumeWithException(Exception("Unexpected result"))
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    
    suspend fun downloadFile(fileId: Int): String = suspendCancellableCoroutine { continuation ->
        try {
            client?.send(TdApi.DownloadFile(fileId, 32, 0, 0, true)) { result ->
                when (result) {
                    is TdApi.File -> {
                        if (result.local.isDownloadingCompleted) {
                            continuation.resume(result.local.path)
                        } else {
                            continuation.resumeWithException(Exception("Download not completed"))
                        }
                    }
                    is TdApi.Error -> continuation.resumeWithException(Exception(result.message))
                    else -> continuation.resumeWithException(Exception("Unexpected result"))
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    suspend fun getMe(): TdApi.User = suspendCancellableCoroutine { continuation ->
        try {
            client?.send(TdApi.GetMe()) { result ->
                when (result) {
                    is TdApi.User -> continuation.resume(result)
                    is TdApi.Error -> continuation.resumeWithException(Exception(result.message))
                    else -> continuation.resumeWithException(Exception("Unexpected result"))
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    fun logout() {
        try {
            client?.send(TdApi.LogOut()) {}
            client = null
            isInitialized = false
        } catch (e: Exception) {
            // Handle logout error
        }
    }
    
    fun isAuthenticated(): Boolean {
        return isInitialized && client != null
    }
}

sealed class AuthState {
    object WaitingForPhoneNumber : AuthState()
    object WaitingForCode : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
