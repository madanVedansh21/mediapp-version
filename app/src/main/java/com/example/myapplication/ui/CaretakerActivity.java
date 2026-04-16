package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityCaretakerBinding;
import com.example.myapplication.model.User;

public class CaretakerActivity extends AppCompatActivity {
    private ActivityCaretakerBinding binding;
    private MediBuddyViewModel viewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaretakerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                binding.etCaretakerName.setText(user.getCaretakerName());
                binding.etCaretakerPhone.setText(user.getCaretakerPhone());
                binding.etCaretakerEmail.setText(user.getCaretakerEmail());
                binding.etHospitalName.setText(user.getHospital());
                binding.etHospitalPhone.setText(user.getHospitalPhone());
                binding.etDoctorName.setText(user.getDoctorName());
                binding.etDoctorPhone.setText(user.getDoctorPhone());
            }
        });

        binding.btnSaveCaretaker.setOnClickListener(v -> {
            if (currentUser != null) {
                User updatedUser = new User(
                        currentUser.getName(),
                        currentUser.getEmail(),
                        currentUser.getPassword(),
                        currentUser.getEmergencyContact(),
                        binding.etCaretakerName.getText().toString(),
                        binding.etCaretakerPhone.getText().toString(),
                        binding.etCaretakerEmail.getText().toString(),
                        binding.etHospitalName.getText().toString(),
                        binding.etHospitalPhone.getText().toString(),
                        binding.etDoctorName.getText().toString(),
                        binding.etDoctorPhone.getText().toString(),
                        currentUser.getDoctorEmail() // Preserving old or add field if needed
                );
                updatedUser.setId(currentUser.getId());
                viewModel.updateUser(updatedUser);
                Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
