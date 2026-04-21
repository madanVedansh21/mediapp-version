package com.example.myapplication.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityDashboardBinding;
import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.Calendar;

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
                binding.tvCaretakerCount.setText(user.getCaretakerName() != null && !user.getCaretakerName().isEmpty() ? "1" : "0");
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

        binding.btnCaretaker.setOnClickListener(v -> {
            startActivity(new Intent(this, CaretakerActivity.class));
        });

        setupProgressTracker();
        setupAnalytics();
        setupMissedDosesStats();

        viewModel.getEmergencyTriggered().observe(this, triggered -> {
            if (triggered) {
                Intent intent = new Intent(this, EmergencyActivity.class);
                startActivity(intent);
                viewModel.resetEmergencyTrigger();
            }
        });

        viewModel.getAllMedicines().observe(this, medicines -> {
            if (medicines != null) {
                binding.tvTotalLogs.setText(String.valueOf(medicines.size()));
            }
        });
    }

    private void setupProgressTracker() {
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

            long startOfToday = getStartOfDay(0);
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

                int displayedTaken = Math.min(dosesTakenToday, finalTotal);
                binding.tvProgressDoses.setText(displayedTaken + " of " + finalTotal + " doses taken Today");
                
                if (finalTotal > 0) {
                    int progress = (int) ((displayedTaken / (float) finalTotal) * 100);
                    binding.progressIndicator.setProgress(progress, true);
                } else {
                    binding.progressIndicator.setProgress(0);
                }
            });
        });
    }

    private void setupAnalytics() {
        viewModel.getAllIntakeLogs().observe(this, logs -> {
            if (logs == null) return;

            ArrayList<BarEntry> takenEntries = new ArrayList<>();
            ArrayList<BarEntry> missedEntries = new ArrayList<>();
            String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            int[] takenCounts = new int[7];
            int[] missedCounts = new int[7];

            Calendar cal = Calendar.getInstance();
            for (IntakeLog log : logs) {
                cal.setTimeInMillis(log.getTimestamp());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                int index = (dayOfWeek + 5) % 7;
                if ("Taken".equalsIgnoreCase(log.getStatus())) {
                    takenCounts[index]++;
                } else if ("Missed".equalsIgnoreCase(log.getStatus())) {
                    missedCounts[index]++;
                }
            }

            for (int i = 0; i < 7; i++) {
                takenEntries.add(new BarEntry(i, takenCounts[i]));
                missedEntries.add(new BarEntry(i, missedCounts[i]));
            }

            BarDataSet takenSet = new BarDataSet(takenEntries, "Taken");
            takenSet.setColor(Color.parseColor("#1A56BE"));
            takenSet.setValueTextColor(Color.BLACK);

            BarDataSet missedSet = new BarDataSet(missedEntries, "Missed");
            missedSet.setColor(Color.parseColor("#FF5252"));
            missedSet.setValueTextColor(Color.BLACK);

            BarData barData = new BarData(takenSet, missedSet);
            float groupSpace = 0.06f;
            float barSpace = 0.02f;
            float barWidth = 0.45f;
            barData.setBarWidth(barWidth);

            binding.barChart.setData(barData);
            binding.barChart.groupBars(0, groupSpace, barSpace);
            
            XAxis xAxis = binding.barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f);
            xAxis.setAxisMinimum(0);
            xAxis.setAxisMaximum(7);
            
            binding.barChart.getAxisLeft().setAxisMinimum(0f);
            binding.barChart.getAxisRight().setEnabled(false);
            binding.barChart.getDescription().setEnabled(false);
            binding.barChart.animateY(1000);
            binding.barChart.invalidate();
        });
    }

    private void setupMissedDosesStats() {
        viewModel.getAllIntakeLogs().observe(this, logs -> {
            int missedToday = 0;
            long startOfToday = getStartOfDay(0);
            if (logs != null) {
                for (IntakeLog log : logs) {
                    if (log.getTimestamp() >= startOfToday && "Missed".equalsIgnoreCase(log.getStatus())) {
                        missedToday++;
                    }
                }
            }
            binding.tvCaretakerCount.setText(String.valueOf(missedToday));
            binding.tvCaretakerLabel.setText("Missed Today");
        });
    }

    private long getStartOfDay(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
