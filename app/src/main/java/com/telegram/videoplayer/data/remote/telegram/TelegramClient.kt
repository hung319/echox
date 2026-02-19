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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
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
    private var currentPhoneNumber: String? = null
    
    // API credentials from BuildConfig (set via environment variables or gradle.properties)
    // Get your credentials from https://my.telegram.org
    private val API_ID = BuildConfig.TELEGRAM_API_ID
    private val API_HASH = BuildConfig.TELEGRAM_API_HASH
    
    suspend fun initialize(phoneNumber: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Initializing Telegram client for phone: $phoneNumber")
            
            // Validate API credentials
            if (API_ID == 0 || API_HASH == "YOUR_API_HASH" || API_HASH.isBlank()) {
                Log.e(TAG, "Invalid Telegram API credentials. Please set TELEGRAM_API_ID and TELEGRAM_API_HASH")
                continuation.resume(Result.failure(Exception("Telegram API credentials not configured. Please set TELEGRAM_API_ID and TELEGRAM_API_HASH in your environment or gradle.properties")))
                return@suspendCancellableCoroutine
            }
            
            currentPhoneNumber = phoneNumber
            
            // Create TDLib settings
            val apiToken = APIToken(API_ID, API_HASH)
            val settings = TDLibSettings.create(apiToken)
            
            // Set database directory
            val dbDir = File(context.filesDir, "tdlib").apply { mkdirs() }
            settings.databaseDirectoryPath = dbDir.toPath()
            
            // Set downloads directory  
            val downloadsDir = File(context.cacheDir, "downloads").apply { mkdirs() }
            settings.downloadedFilesDirectoryPath = downloadsDir.toPath()
            
            Log.d(TAG, "Database path: ${dbDir.absolutePath}")
            
            // Create auth supplier
            val authSupplier = AuthenticationSupplier { clientId ->
                Log.d(TAG, "Auth supplier called for clientId: $clientId")
                TdApi.PhoneNumberAuthenticationSettings().apply {
                    allowFlashCall = false
                    isCurrentPhoneNumber = false
                    allowSmsRetrieverApi = false
                }
            }
            
            // Create and start client
            client = SimpleTelegramClient(settings)
            
            Log.d(TAG, "Starting Telegram client...")
            client?.start(authSupplier)
            
            isInitialized = true
            Log.d(TAG, "Telegram client initialized successfully")
            continuation.resume(Result.success(Unit))
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Telegram client", e)
            isInitialized = false
            client = null
            continuation.resume(Result.failure(e))
        }
    }
    
    suspend fun sendCode(phoneNumber: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "sendCode called with: $phoneNumber")
            
            if (client == null) {
                Log.e(TAG, "Client not initialized, initializing first...")
                val initResult = initialize(phoneNumber)
                if (initResult.isFailure) {
                    continuation.resume(initResult)
                    return@suspendCancellableCoroutine
                }
            }
            
            val formattedPhone = formatPhoneNumber(phoneNumber)
            Log.d(TAG, "Formatted phone: $formattedPhone")
            
            client?.send(TdApi.SetAuthenticationPhoneNumber(formattedPhone, null)) { result ->
                Log.d(TAG, "sendCode result: $result")
                when (result) {
                    is TdApi.Ok -> {
                        Log.d(TAG, "sendCode success")
                        continuation.resume(Result.success(Unit))
                    }
                    is TdApi.Error -> {
                        Log.e(TAG, "sendCode error: ${result.message}")
                        continuation.resume(Result.failure(Exception(result.message)))
                    }
                    else -> {
                        Log.e(TAG, "sendCode unexpected result: $result")
                        continuation.resume(Result.failure(Exception("Unexpected result: $result")))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendCode exception", e)
            continuation.resumeWithException(e)
        }
    }
    
    suspend fun checkCode(code: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "checkCode called with: $code")
            
            if (client == null) {
                Log.e(TAG, "Client not initialized")
                continuation.resume(Result.failure(Exception("Client not initialized")))
                return@suspendCancellableCoroutine
            }
            
            client?.send(TdApi.CheckAuthenticationCode(code)) { result ->
                Log.d(TAG, "checkCode result: $result")
                when (result) {
                    is TdApi.Ok -> {
                        Log.d(TAG, "checkCode success")
                        continuation.resume(Result.success(Unit))
                    }
                    is TdApi.Error -> {
                        Log.e(TAG, "checkCode error: ${result.message}")
                        continuation.resume(Result.failure(Exception(result.message)))
                    }
                    else -> {
                        Log.e(TAG, "checkCode unexpected result")
                        continuation.resume(Result.failure(Exception("Unexpected result")))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkCode exception", e)
            continuation.resumeWithException(e)
        }
    }
    
    suspend fun getChats(): List<TdApi.Chat> = suspendCancellableCoroutine { continuation ->
        try {
            if (client == null) {
                continuation.resumeWithException(Exception("Client not initialized"))
                return@suspendCancellableCoroutine
            }
            
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
            currentPhoneNumber = null
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
        }
    }
    
    fun isAuthenticated(): Boolean {
        return isInitialized && client != null
    }
    
    private fun formatPhoneNumber(phone: String): String {
        var formatted = phone.replace(Regex("[^0-9+]"), "")
        if (!formatted.startsWith("+")) {
            formatted = "+$formatted"
        }
        return formatted
    }
}

sealed class AuthState {
    object WaitingForPhoneNumber : AuthState()
    object WaitingForCode : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
