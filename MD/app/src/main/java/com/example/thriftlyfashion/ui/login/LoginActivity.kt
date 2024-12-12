package com.example.thriftlyfashion.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.ui.MainActivity
import com.example.thriftlyfashion.ui.signup.SignupActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var ivTogglePassword: ImageView
    private lateinit var tvForgetPassword: TextView
    private lateinit var tvRegister: TextView

    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        tvForgetPassword = findViewById(R.id.tvForgetPassword)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener { handleLogin() }
        ivTogglePassword.setOnClickListener { togglePasswordVisibility() }
        tvForgetPassword.setOnClickListener {
            Toast.makeText(this, "Lupa password? Fitur ini belum tersedia.", Toast.LENGTH_SHORT).show()
        }
        tvRegister.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "Password harus memiliki minimal 8 karakter!", Toast.LENGTH_SHORT).show()
            return
        }

        if (username == "user" && password == "password") {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Username atau password salah.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivTogglePassword.setImageResource(R.drawable.baseline_visibility_24)
        } else {
            etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
            ivTogglePassword.setImageResource(R.drawable.baseline_visibility_off_24)
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text.length)
    }
}