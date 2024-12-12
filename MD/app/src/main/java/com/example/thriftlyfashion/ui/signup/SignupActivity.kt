package com.example.thriftlyfashion.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.ui.login.LoginActivity

class SignupActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setupView()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        val ivConfirmPasswordToggle = findViewById<ImageView>(R.id.ivConfirPasswordToggle)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val backButton = findViewById<Button>(R.id.backButton)

        val etNama = findViewById<EditText>(R.id.etNama)
        val email = findViewById<EditText>(R.id.email)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirPassword)

        ivTogglePassword.setOnClickListener {
            togglePasswordVisibility(etPassword, ivTogglePassword)
        }

        ivConfirmPasswordToggle.setOnClickListener {
            toggleConfirmPasswordVisibility(etConfirmPassword, ivConfirmPasswordToggle)
        }

        signupButton.setOnClickListener {
            val name = etNama.text.toString().trim()
            val emailText = email.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty() || emailText.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password harus memiliki minimal 8 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password atau konfirmasi password tidak cocok.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this).apply {
                setTitle("Pendaftaran Berhasil!")
                setMessage("Akun untuk $emailText telah berhasil dibuat.")
                setPositiveButton("OK") { _, _ ->
                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                create()
                show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun togglePasswordVisibility(passwordField: EditText, toggleButton: ImageView) {
        isPasswordVisible = !isPasswordVisible
        passwordField.inputType = if (isPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        passwordField.setSelection(passwordField.text.length)
        toggleButton.setImageResource(
            if (isPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
        )
    }

    private fun toggleConfirmPasswordVisibility(confirmPasswordField: EditText, toggleButton: ImageView) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        confirmPasswordField.inputType = if (isConfirmPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        confirmPasswordField.setSelection(confirmPasswordField.text.length)
        toggleButton.setImageResource(
            if (isConfirmPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
        )
    }
}
