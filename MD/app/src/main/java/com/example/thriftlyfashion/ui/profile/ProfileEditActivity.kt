package com.example.thriftlyfashion.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.SharedPrefManager
import com.google.android.material.textfield.TextInputEditText

class ProfileEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_edit)

        setupInsets()
        setupListeners()
        loadProfileData()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupListeners() {
        findViewById<View>(R.id.id_btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.id_simpanButton).setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        val username = findViewById<TextInputEditText>(R.id.editTextUsername)?.text.toString()
        val email = findViewById<TextInputEditText>(R.id.editText)?.text.toString()
        val gender = findViewById<TextInputEditText>(R.id.editText2)?.text.toString()
        val birthDate = findViewById<TextInputEditText>(R.id.editText3)?.text.toString()
        val phoneNumber = findViewById<TextInputEditText>(R.id.editText5)?.text.toString()

        if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "tidak ada yang boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        saveProfileToPreferences(username, email, gender, birthDate, phoneNumber)
    }

    private fun saveProfileToPreferences(username: String, email: String, gender: String, birthDate: String, phoneNumber: String) {
        val sharedPrefManager = SharedPrefManager(this)
        sharedPrefManager.saveUserName(username)
        sharedPrefManager.saveUserEmail(email)
        sharedPrefManager.saveUserPhoneNumber(phoneNumber)

        Toast.makeText(this, "Profil berhasil disimpan secara lokal", Toast.LENGTH_SHORT).show()
    }

    private fun loadProfileData() {
        val sharedPrefManager = SharedPrefManager(this)

        val username = sharedPrefManager.getUserName()
        val email = sharedPrefManager.getUserEmail()
        val phoneNumber = sharedPrefManager.getUserPhoneNumber()

        findViewById<TextInputEditText>(R.id.editTextUsername)?.setText(username)
        findViewById<TextInputEditText>(R.id.editText)?.setText(email)
        findViewById<TextInputEditText>(R.id.editText5)?.setText(phoneNumber)

        val gender = "-"
        val birthDate = "-"
        findViewById<TextInputEditText>(R.id.editText2)?.setText(gender)
        findViewById<TextInputEditText>(R.id.editText3)?.setText(birthDate)
    }
}
