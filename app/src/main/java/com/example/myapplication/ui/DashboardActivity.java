package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityDashboardBinding;
import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.model.User;

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

        binding.btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, LogHistoryActivity.class));
        });

        setupProgressTracker();
    }

    private void setupProgressTracker() {
        // Observe all medicines and intake logs to calculate progress
        viewModel.getAllMedicines().observe(this, medicines -> {
            if (medicines == null || medicines.isEmpty()) {
                binding.tvProgressDoses.setText("No medicines scheduled");
                binding.progressIndicator.setProgress(0);
                return;
            }

            int totalExpectedDoses = 0;
            for (Medicine med : medicines) {
                totalExpectedDoses += med.getFrequency();
            }

            // Get logs for today
            long startOfToday = getStartOfDay();
            final int finalTotal = totalExpectedDoses;
            
            viewModel.getAllIntakeLogs().observe(this, logs -> {
                int dosesTakenToday = 0;
                if (logs != null) {
                    for (IntakeLog log : logs) {
                        if (log.getTimestamp() >= startOfToday && "Taken".equalsIgnoreCase(log.getStatus())) {
                            dosesTakenToday++;
                        }
                    }
                }

                binding.tvProgressDoses.setText(dosesTakenToday + " of " + finalTotal + " doses taken");
                if (finalTotal > 0) {
                    int progress = (int) ((dosesTakenToday / (float) finalTotal) * 100);
                    binding.progressIndicator.setProgress(progress, true);
                } else {
                    binding.progressIndicator.setProgress(0);
                }
            });
        });
    }

    private long getStartOfDay() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
