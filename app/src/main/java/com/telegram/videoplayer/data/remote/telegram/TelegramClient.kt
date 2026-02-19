package com.telegram.videoplayer.data.remote.telegram

import android.content.Context
import android.util.Log
import com.telegram.videoplayer.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationSupplier
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.jni.TdApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TelegramClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TelegramClient"
    }
    
    private var client: SimpleTelegramClient? = null
    private var isInitialized = false
    
    private val API_ID = BuildConfig.TELEGRAM_API_ID
    private val API_HASH = BuildConfig.TELEGRAM_API_HASH
    
    private suspend fun doInitialize(phoneNumber: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing Telegram client for phone: $phoneNumber")
            
            if (API_ID == 0 || API_HASH == "YOUR_API_HASH" || API_HASH.isBlank()) {
                return@withContext Result.failure(Exception("Telegram API credentials not configured"))
            }
            
            val apiToken = APIToken(API_ID, API_HASH)
            val settings = TDLibSettings.create(apiToken)
            
            val dbDir = File(context.filesDir, "tdlib").apply { mkdirs() }
            settings.databaseDirectoryPath = dbDir.toPath()
            
            val downloadsDir = File(context.cacheDir, "downloads").apply { mkdirs() }
            settings.downloadedFilesDirectoryPath = downloadsDir.toPath()
            
            val authSupplier = AuthenticationSupplier {
                TdApi.PhoneNumberAuthenticationSettings().apply {
                    allowFlashCall = false
                    isCurrentPhoneNumber = false
                    allowSmsRetrieverApi = false
                }
            }
            
            client = SimpleTelegramClient(settings)
            client?.start(authSupplier)
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            isInitialized = false
            client = null
            Result.failure(e)
        }
    }
    
    suspend fun sendCode(phoneNumber: String): Result<Unit> {
        if (client == null || !isInitialized) {
            val initResult = doInitialize(phoneNumber)
            if (initResult.isFailure) return initResult
        }
        
        return suspendCancellableCoroutine { continuation ->
            try {
                val formattedPhone = formatPhoneNumber(phoneNumber)
                client?.send(TdApi.SetAuthenticationPhoneNumber(formattedPhone, null)) { result ->
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
    }
    
    suspend fun checkCode(code: String): Result<Unit> {
        if (client == null) return Result.failure(Exception("Client not initialized"))
        
        return suspendCancellableCoroutine { continuation ->
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
    }
    
    suspend fun getMe(): TdApi.User {
        if (client == null) throw Exception("Client not initialized")
        
        return suspendCancellableCoroutine { continuation ->
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
    }
    
    suspend fun getChats(): List<TdApi.Chat> {
        if (client == null) return emptyList()
        
        return suspendCancellableCoroutine { continuation ->
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
    }
    
    suspend fun getChatHistory(chatId: Long, limit: Int = 100): List<TdApi.Message> {
        if (client == null) return emptyList()
        
        return suspendCancellableCoroutine { continuation ->
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
    }
    
    suspend fun downloadFile(fileId: Int): String {
        if (client == null) throw Exception("Client not initialized")
        
        return suspendCancellableCoroutine { continuation ->
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
    }
    
    fun logout() {
        try {
            client?.send(TdApi.LogOut()) {}
            client = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
        }
    }
    
    fun isAuthenticated(): Boolean = isInitialized && client != null
    
    private fun formatPhoneNumber(phone: String): String {
        var formatted = phone.replace(Regex("[^0-9+]"), "")
        if (!formatted.startsWith("+")) formatted = "+$formatted"
        return formatted
    }
}

sealed class AuthState {
    object WaitingForPhoneNumber : AuthState()
    object WaitingForCode : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
