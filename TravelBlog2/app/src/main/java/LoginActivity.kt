package com.travelblog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.travelblog.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener { onLoginClicked() }

        binding.textUsernameLayout.editText?.addTextChangedListener(createTextWatcher(binding.textUsernameLayout))
        binding.textPasswordInput.editText?.addTextChangedListener(createTextWatcher(binding.textPasswordInput))
    }

    private fun onLoginClicked() {
        val username: String = binding.textUsernameLayout.editText?.text.toString()
        val password: String = binding.textPasswordInput.editText?.text.toString()

        if (username.isEmpty()) {
            binding.textUsernameLayout.error = "Username must not be empty"
        } else if (password.isEmpty()) {
            binding.textPasswordInput.error = "Password must not be empty"
        } else if (username != "admin" && password != "admin") {
            showErrorDialog()
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Login Failed")
            .setMessage("Username or password is not correct. Please try again.")
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss()}
            .show()

    }

    private fun createTextWatcher(textPasswordInput: TextInputLayout): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence,
                                           start: Int, count: Int, after: Int) {
                // not needed
            }

            override fun onTextChanged(s: CharSequence,
                                       start: Int, before: Int, count: Int) {
                textPasswordInput.error = null
            }

            override fun afterTextChanged(s: Editable) {
                // not needed
            }
        }
    }
}