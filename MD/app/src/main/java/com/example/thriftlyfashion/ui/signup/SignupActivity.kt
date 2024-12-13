package com.example.thriftlyfashion.ui.signup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.thriftlyfashion.R;
import com.example.thriftlyfashion.remote.api.ApiService;
import com.example.thriftlyfashion.remote.api.RetrofitClient;
import com.example.thriftlyfashion.remote.model.SignupRequest;
import com.example.thriftlyfashion.ui.login.LoginActivity;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.launch;
import kotlinx.coroutines.withContext;

class SignupActivity : AppCompatActivity() {
    private var isPasswordVisible = false;
    private var isConfirmPasswordVisible = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setupView();
        setupAction();
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars());
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
        supportActionBar?.hide();
    }

    private fun setupAction() {
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword);
        val ivConfirmPasswordToggle = findViewById<ImageView>(R.id.ivConfirPasswordToggle);
        val signupButton = findViewById<Button>(R.id.signupButton);
        val backButton = findViewById<Button>(R.id.backButton);

        val etNama = findViewById<EditText>(R.id.etNameStore);
        val etEmail = findViewById<EditText>(R.id.email);
        val etPassword = findViewById<EditText>(R.id.etPassword);
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirPassword);
        val etNumberPhone = findViewById<EditText>(R.id.etNumberPhone);

        ivTogglePassword.setOnClickListener {
            togglePasswordVisibility(etPassword, ivTogglePassword);
        };

        ivConfirmPasswordToggle.setOnClickListener {
            toggleConfirmPasswordVisibility(etConfirmPassword, ivConfirmPasswordToggle);
        };

        signupButton.setOnClickListener {
            val name = etNama.text.toString().trim();
            val email = etEmail.text.toString().trim();
            val password = etPassword.text.toString();
            val confirmPassword = etConfirmPassword.text.toString();
            val numberPhone = etNumberPhone.text.toString().trim();

            if (numberPhone.isEmpty() || numberPhone.length < 10 || !numberPhone.matches(Regex("^[0-9]+$"))) {
                Toast.makeText(this, "Nomor telepon tidak valid!", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (name.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email tidak valid!", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (password.isEmpty() || password.length < 8) {
                Toast.makeText(this, "Password harus memiliki minimal 8 karakter!", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (confirmPassword.isEmpty() || password != confirmPassword) {
                Toast.makeText(this, "Password dan konfirmasi password tidak sama!", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            val signupRequest = SignupRequest(name, email, password, numberPhone);

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        val apiService = RetrofitClient.createService(ApiService::class.java);
                        apiService.registerUser(signupRequest);
                    };

                    if (response.isSuccessful) {
                        showCustomConfirmationDialog();
                    } else {
                        Toast.makeText(this@SignupActivity, "Gagal membuat akun: ${response.message()}", Toast.LENGTH_SHORT).show();
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SignupActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show();
                }
            };
        };

        backButton.setOnClickListener {
            finish();
        };
    }

    private fun showCustomConfirmationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null);
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create();

        dialogView.findViewById<Button>(R.id.btnYes).setOnClickListener {
            val intent = Intent(this, SignupOwnerActivity::class.java);
            startActivity(intent);
            finish();
            dialog.dismiss();
        };

        dialogView.findViewById<Button>(R.id.btnNo).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java);
            startActivity(intent);
            finish();
            dialog.dismiss();
        };

        dialog.show();
    }

    private fun togglePasswordVisibility(passwordField: EditText, toggleButton: ImageView) {
        isPasswordVisible = !isPasswordVisible;
        passwordField.inputType = if (isPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;
        };
        passwordField.setSelection(passwordField.text.length);

        toggleButton.setImageResource(
            if (isPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
        );
    }

    private fun toggleConfirmPasswordVisibility(confirmPasswordField: EditText, toggleButton: ImageView) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        confirmPasswordField.inputType = if (isConfirmPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;
        };

        confirmPasswordField.setSelection(confirmPasswordField.text.length);
        toggleButton.setImageResource(
            if (isConfirmPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
        );
    }
}