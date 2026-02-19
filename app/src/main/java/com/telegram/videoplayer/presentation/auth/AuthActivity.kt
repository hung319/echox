package com.telegram.videoplayer.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
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
    
    private var isWaitingForCode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        observeStates()
    }
    
    private fun setupViews() {
        // Country code formatting
        binding.countryCodeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty() && !text.startsWith("+")) {
                    binding.countryCodeInput.setText("+$text")
                    binding.countryCodeInput.setSelection(binding.countryCodeInput.text?.length ?: 0)
                }
            }
        })
        
        // Continue button - send phone number
        binding.continueButton.setOnClickListener {
            val countryCode = binding.countryCodeInput.text.toString().trim()
            val phoneNumber = binding.phoneNumberInput.text.toString().trim()
            
            if (countryCode.isEmpty()) {
                showError(getString(R.string.error_invalid_phone))
                return@setOnClickListener
            }
            
            if (phoneNumber.isEmpty()) {
                showError(getString(R.string.error_empty_phone))
                return@setOnClickListener
            }
            
            // Combine country code and phone number
            val fullPhoneNumber = countryCode + phoneNumber
            viewModel.sendCode(fullPhoneNumber)
        }
        
        // Verify button - check code
        binding.verifyButton.setOnClickListener {
            val code = binding.codeInput.text.toString().trim()
            
            if (code.isEmpty()) {
                showError(getString(R.string.error_empty_code))
                return@setOnClickListener
            }
            
            viewModel.checkCode(code)
        }
        
        // Enter key on code input
        binding.codeInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.verifyButton.performClick()
                true
            } else {
                false
            }
        }
        
        // Enter key on phone input
        binding.phoneNumberInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.continueButton.performClick()
                true
            } else {
                false
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
                                showLoading(true)
                                binding.continueButton.isEnabled = false
                                binding.countryCodeInput.isEnabled = false
                                binding.phoneNumberInput.isEnabled = false
                            }
                            is UiState.Success -> {
                                showLoading(false)
                                showCodeInput()
                            }
                            is UiState.Error -> {
                                showLoading(false)
                                binding.continueButton.isEnabled = true
                                binding.countryCodeInput.isEnabled = true
                                binding.phoneNumberInput.isEnabled = true
                                showError(state.message)
                                viewModel.resetPhoneState()
                            }
                            else -> {
                                showLoading(false)
                                binding.continueButton.isEnabled = true
                                binding.countryCodeInput.isEnabled = true
                                binding.phoneNumberInput.isEnabled = true
                            }
                        }
                    }
                }
                
                launch {
                    viewModel.codeState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                showLoading(true)
                                binding.verifyButton.isEnabled = false
                                binding.codeInput.isEnabled = false
                            }
                            is UiState.Success -> {
                                showLoading(false)
                                navigateToMain()
                            }
                            is UiState.Error -> {
                                showLoading(false)
                                binding.verifyButton.isEnabled = true
                                binding.codeInput.isEnabled = true
                                showError(state.message)
                                viewModel.resetCodeState()
                            }
                            else -> {
                                showLoading(false)
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
        binding.phoneNumberLayout.visibility = View.GONE
        binding.continueButton.visibility = View.GONE
        
        binding.codeLayout.visibility = View.VISIBLE
        binding.codeHintText.visibility = View.VISIBLE
        binding.verifyButton.visibility = View.VISIBLE
        
        isWaitingForCode = true
        
        // Focus on code input
        binding.codeInput.requestFocus()
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error_auth)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
