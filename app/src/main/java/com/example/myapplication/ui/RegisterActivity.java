package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.databinding.ActivityRegisterBinding;
import com.example.myapplication.model.User;

import com.example.myapplication.util.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "medibuddy_prefs";
    private static final String KEY_LOGGED_IN_EMAIL = "logged_in_email";
    private ActivityRegisterBinding binding;
    private MediBuddyViewModel viewModel;
    private static final String[] COUNTRY_CODES = {
            "+1", "+44", "+61", "+65", "+81", "+91", "+971"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);
        ArrayAdapter<String> countryCodeAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            COUNTRY_CODES
        );
        binding.actvCountryCode.setAdapter(countryCodeAdapter);
        binding.actvCountryCode.setText("+91", false);

        binding.btnRegister.setOnClickListener(v -> {
            // 1. Sanitize inputs
            String name = ValidationUtils.sanitize(binding.etName.getText().toString());
            String email = ValidationUtils.sanitize(binding.etEmail.getText().toString()).toLowerCase();
            String password = ValidationUtils.sanitize(binding.etPassword.getText().toString());
            String emergencyContact = ValidationUtils.sanitize(binding.etEmergencyContact.getText().toString());
            String caretakerName = ValidationUtils.sanitize(binding.etCaretakerName.getText().toString());
            String caretakerPhone = ValidationUtils.sanitize(binding.etCaretakerPhone.getText().toString());
            String caretakerEmail = ValidationUtils.sanitize(binding.etCaretakerEmail.getText().toString()).toLowerCase();
            String countryCode = ValidationUtils.sanitize(binding.actvCountryCode.getText().toString());
            if (countryCode.isEmpty()) {
                countryCode = "+91";
            }

            // 2. Comprehensive Validation
            if (!ValidationUtils.isValidName(name)) {
                binding.etName.setError("Name must be 3-20 alphabetic characters");
                binding.etName.requestFocus();
                return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                binding.etEmail.setError("Enter a valid email address");
                binding.etEmail.requestFocus();
                return;
            }

            if (!ValidationUtils.isValidPassword(password)) {
                binding.etPassword.setError("Password must be at least 6 characters");
                binding.etPassword.requestFocus();
                return;
            }

            String normalizedEmergencyPhone = ValidationUtils.normalizePhoneWithCountryCode(countryCode, emergencyContact);
            if (normalizedEmergencyPhone == null) {
                binding.etEmergencyContact.setError("Enter exactly 10 digits");
                binding.etEmergencyContact.requestFocus();
                return;
            }

            // Caretaker optional field validation if provided
            if (!caretakerName.isEmpty() && !ValidationUtils.isValidName(caretakerName)) {
                binding.etCaretakerName.setError("Name must be alphabetic");
                binding.etCaretakerName.requestFocus();
                return;
            }

            String normalizedCaretakerPhone = "";
            if (!caretakerPhone.isEmpty()) {
                normalizedCaretakerPhone = ValidationUtils.normalizePhoneWithCountryCode(countryCode, caretakerPhone);
            }
            if (!caretakerPhone.isEmpty() && normalizedCaretakerPhone == null) {
                binding.etCaretakerPhone.setError("Enter exactly 10 digits");
                binding.etCaretakerPhone.requestFocus();
                return;
            }

            if (!caretakerEmail.isEmpty() && !ValidationUtils.isValidEmail(caretakerEmail)) {
                binding.etCaretakerEmail.setError("Enter a valid email address");
                binding.etCaretakerEmail.requestFocus();
                return;
            }

            binding.btnRegister.setEnabled(false);
            new Thread(() -> {
                User existingUser = AppDatabase.getDatabase(this).userDao().getUserByEmail(email);
                runOnUiThread(() -> {
                    if (existingUser != null) {
                        binding.btnRegister.setEnabled(true);
                        binding.etEmail.setError("This email is already registered. Please login.");
                        binding.etEmail.requestFocus();
                        Toast.makeText(this, "Account already exists for this email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. Data Integrity Enforcement (Backend-like creation)
                    User user = new User(
                            name,
                            email,
                            password,
                            normalizedEmergencyPhone,
                            caretakerName,
                            normalizedCaretakerPhone == null ? "" : normalizedCaretakerPhone,
                            caretakerEmail,
                            "City Hospital",
                            "",
                            "",
                            "",
                            ""
                    );
                    viewModel.register(user);

                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_LOGGED_IN_EMAIL, user.getEmail())
                            .apply();

                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                });
            }).start();
        });
    }
}
