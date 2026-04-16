package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private MediBuddyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                binding.tvUserName.setText(user.getName());
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        binding.btnAddMedicine.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMedicineActivity.class));
        });

        binding.btnMedicineList.setOnClickListener(v -> {
            startActivity(new Intent(this, MedicineListActivity.class));
        });

        binding.btnSymptomLog.setOnClickListener(v -> {
            startActivity(new Intent(this, SymptomLogActivity.class));
        });

        binding.btnEmergency.setOnClickListener(v -> {
            startActivity(new Intent(this, EmergencyActivity.class));
        });
    }
}
