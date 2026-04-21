package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivitySymptomLogBinding;
import com.example.myapplication.model.SymptomLog;

public class SymptomLogActivity extends AppCompatActivity {
    private ActivitySymptomLogBinding binding;
    private MediBuddyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySymptomLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        viewModel.getEmergencyTriggered().observe(this, triggered -> {
            if (triggered) {
                android.content.Intent intent = new android.content.Intent(this, EmergencyActivity.class);
                startActivity(intent);
                viewModel.resetEmergencyTrigger();
                finish();
            }
        });

        binding.btnLogSymptom.setOnClickListener(v -> {
            String category = binding.etSymptomCategory.getText().toString();
            int severity = binding.sbSeverity.getProgress();
            String notes = binding.etSymptomNotes.getText().toString();


            if (category.isEmpty()) {
                Toast.makeText(this, "Please enter a category", Toast.LENGTH_SHORT).show();
                return;
            }

            SymptomLog log = new SymptomLog(category, severity, notes, System.currentTimeMillis());
            viewModel.logSymptom(log);
            
            if (severity < 7) {
                showAyurvedicSuggestion(category);
            } else {
                viewModel.checkSymptomSeverity(); // Background check for critical condition
            }

            Toast.makeText(this, "Symptom Logged", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showAyurvedicSuggestion(String category) {
        String suggestion;
        if (category.toLowerCase().contains("headache")) {
            suggestion = "Apply ginger paste or drink chamomile tea.";
        } else if (category.toLowerCase().contains("stomach") || category.toLowerCase().contains("digestion")) {
            suggestion = "Drink warm fennel water or chew carom seeds (Ajwain).";
        } else if (category.toLowerCase().contains("fever")) {
            suggestion = "Rest and drink Tulsi (Basil) tea.";
        } else if (category.toLowerCase().contains("cough") || category.toLowerCase().contains("cold")) {
            suggestion = "Take honey with a pinch of turmeric and ginger.";
        } else {
            suggestion = "Try drinking warm ginger water for " + category + ". Maintain hydration.";
        }

        com.example.myapplication.util.NotificationHelper.showCustomNotification(
                this, 
                "Ayurvedic Suggestion",
                suggestion, 
                android.R.drawable.ic_menu_today, 
                R.color.secondaryColor);
    }
}
