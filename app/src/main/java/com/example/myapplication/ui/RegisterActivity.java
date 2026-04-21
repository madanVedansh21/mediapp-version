package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityRegisterBinding;
import com.example.myapplication.model.User;

import com.example.myapplication.util.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private MediBuddyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            // 1. Sanitize inputs
            String name = ValidationUtils.sanitize(binding.etName.getText().toString());
            String email = ValidationUtils.sanitize(binding.etEmail.getText().toString()).toLowerCase();
            String password = ValidationUtils.sanitize(binding.etPassword.getText().toString());
            String emergencyContact = ValidationUtils.sanitize(binding.etEmergencyContact.getText().toString());
            String caretakerName = ValidationUtils.sanitize(binding.etCaretakerName.getText().toString());
            String caretakerPhone = ValidationUtils.sanitize(binding.etCaretakerPhone.getText().toString());
            String caretakerEmail = ValidationUtils.sanitize(binding.etCaretakerEmail.getText().toString()).toLowerCase();

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

            if (!ValidationUtils.isValidPhone(emergencyContact)) {
                binding.etEmergencyContact.setError("Enter a valid 10-digit phone number");
                binding.etEmergencyContact.requestFocus();
                return;
            }

            // Caretaker optional field validation if provided
            if (!caretakerName.isEmpty() && !ValidationUtils.isValidName(caretakerName)) {
                binding.etCaretakerName.setError("Name must be alphabetic");
                binding.etCaretakerName.requestFocus();
                return;
            }

            if (!caretakerPhone.isEmpty() && !ValidationUtils.isValidPhone(caretakerPhone)) {
                binding.etCaretakerPhone.setError("Enter a valid 10-digit phone number");
                binding.etCaretakerPhone.requestFocus();
                return;
            }

            if (!caretakerEmail.isEmpty() && !ValidationUtils.isValidEmail(caretakerEmail)) {
                binding.etCaretakerEmail.setError("Enter a valid email address");
                binding.etCaretakerEmail.requestFocus();
                return;
            }

            // 3. Data Integrity Enforcement (Backend-like creation)
            User user = new User(name, email, password, emergencyContact, caretakerName, caretakerPhone, caretakerEmail, "City Hospital", "", "", "", "");
            viewModel.register(user);
            
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }
}
