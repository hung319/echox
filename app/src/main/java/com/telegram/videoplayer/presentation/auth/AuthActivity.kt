package com.telegram.videoplayer.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telegram.videoplayer.R
import com.telegram.videoplayer.databinding.ActivityAuthBinding
import com.telegram.videoplayer.presentation.MainActivity
import com.telegram.videoplayer.presentation.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    
    private var currentPhoneNumber: String = ""
    private var isWaitingForCode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        observeStates()
    }
    
    private fun setupViews() {
        binding.continueButton.setOnClickListener {
            val phoneNumber = binding.phoneNumberInput.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                currentPhoneNumber = phoneNumber
                viewModel.sendCode(phoneNumber)
            } else {
                showError("Please enter a phone number")
            }
        }
        
        binding.verifyButton.setOnClickListener {
            val code = binding.codeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                viewModel.checkCode(code)
            } else {
                showError("Please enter the verification code")
            }
        }
    }
    
    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.phoneState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.continueButton.isEnabled = false
                                binding.phoneNumberInput.isEnabled = false
                            }
                            is UiState.Success -> {
                                showCodeInput()
                            }
                            is UiState.Error -> {
                                binding.continueButton.isEnabled = true
                                binding.phoneNumberInput.isEnabled = true
                                showError(state.message)
                                viewModel.resetPhoneState()
                            }
                            else -> {
                                binding.continueButton.isEnabled = true
                                binding.phoneNumberInput.isEnabled = true
                            }
                        }
                    }
                }
                
                launch {
                    viewModel.codeState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.verifyButton.isEnabled = false
                                binding.codeInput.isEnabled = false
                            }
                            is UiState.Success -> {
                                navigateToMain()
                            }
                            is UiState.Error -> {
                                binding.verifyButton.isEnabled = true
                                binding.codeInput.isEnabled = true
                                showError(state.message)
                                viewModel.resetCodeState()
                            }
                            else -> {
                                binding.verifyButton.isEnabled = true
                                binding.codeInput.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun showCodeInput() {
        binding.phoneNumberLayout.visibility = android.view.View.GONE
        binding.continueButton.visibility = android.view.View.GONE
        binding.codeLayout.visibility = android.view.View.VISIBLE
        binding.verifyButton.visibility = android.view.View.VISIBLE
        isWaitingForCode = true
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
