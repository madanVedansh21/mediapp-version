package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
        String suggestion = "Try drinking warm ginger water for " + category;
        Toast.makeText(this, "Suggestion: " + suggestion, Toast.LENGTH_LONG).show();
    }
}
