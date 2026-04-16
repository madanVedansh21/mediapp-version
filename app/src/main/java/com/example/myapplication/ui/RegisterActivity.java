package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityRegisterBinding;
import com.example.myapplication.model.User;

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
            String name = binding.etName.getText().toString();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            String emergencyContact = binding.etEmergencyContact.getText().toString();
            String caretakerName = binding.etCaretakerName.getText().toString();
            String caretakerPhone = binding.etCaretakerPhone.getText().toString();
            String caretakerEmail = binding.etCaretakerEmail.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || emergencyContact.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User(name, email, password, emergencyContact, caretakerName, caretakerPhone, caretakerEmail, "City Hospital");
            viewModel.register(user);
            
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }
}
