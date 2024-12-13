package com.example.thriftlyfashion.ui.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.ui.AddProductOwner.AddProductActivity

class SignupOwnerActivity : AppCompatActivity() {

    private lateinit var storeName: EditText
    private lateinit var storeOwner: EditText
    private lateinit var numberBusiness: EditText
    private lateinit var businessEmail: EditText
    private lateinit var backButton: Button
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_owner)

        storeName = findViewById(R.id.storeName)
        storeOwner = findViewById(R.id.storeOwner)
        numberBusiness = findViewById(R.id.numberBusiness)
        businessEmail = findViewById(R.id.businessEmail)
        backButton = findViewById(R.id.backButton)
        signupButton = findViewById(R.id.signupButton)

        backButton.setOnClickListener {
            finish()
        }

        signupButton.setOnClickListener {
            val intent = Intent(this@SignupOwnerActivity, AddProductActivity::class.java)
            startActivity(intent)
            finish()
        }

        businessEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isValidEmail(s.toString())) {
                    businessEmail.error = "Email tidak valid"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (storeName.text.isNullOrEmpty()) {
            storeName.error = "Nama toko tidak boleh kosong"
            isValid = false
        }

        if (storeOwner.text.isNullOrEmpty()) {
            storeOwner.error = "Nama pemilik tidak boleh kosong"
            isValid = false
        }

        if (numberBusiness.text.isNullOrEmpty()) {
            numberBusiness.error = "Nomor bisnis tidak boleh kosong"
            isValid = false
        }

        if (businessEmail.text.isNullOrEmpty() || !isValidEmail(businessEmail.text.toString())) {
            businessEmail.error = "Email tidak valid"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
