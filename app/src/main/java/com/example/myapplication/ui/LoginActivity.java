package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.databinding.ActivityLoginBinding;
import com.example.myapplication.model.User;

import com.example.myapplication.util.ValidationUtils;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "medibuddy_prefs";
    private static final String KEY_LOGGED_IN_EMAIL = "logged_in_email";
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> {
            String email = ValidationUtils.sanitize(binding.etEmail.getText().toString()).toLowerCase();
            String password = ValidationUtils.sanitize(binding.etPassword.getText().toString());

            if (!ValidationUtils.isValidEmail(email)) {
                binding.etEmail.setError("Enter a valid email address");
                binding.etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.etPassword.setError("Password cannot be empty");
                binding.etPassword.requestFocus();
                return;
            }

            // Simple mock login logic
            new Thread(() -> {
                User user = AppDatabase.getDatabase(this).userDao().login(email, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                .edit()
                                .putString(KEY_LOGGED_IN_EMAIL, user.getEmail())
                                .apply();
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        binding.btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
