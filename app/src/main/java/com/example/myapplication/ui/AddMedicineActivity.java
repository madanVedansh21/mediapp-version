package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityAddMedicineBinding;
import com.example.myapplication.model.Medicine;

public class AddMedicineActivity extends AppCompatActivity {
    private ActivityAddMedicineBinding binding;
    private MediBuddyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        binding.btnSaveMedicine.setOnClickListener(v -> {
            String name = binding.etMedName.getText().toString();
            String type = binding.spnMedType.getSelectedItem().toString();
            String dosage = binding.etDosage.getText().toString();
            String frequencyStr = binding.etFrequency.getText().toString();
            String stockStr = binding.etStock.getText().toString();
            String thresholdStr = binding.etThreshold.getText().toString();
            String schedule = binding.etSchedule.getText().toString();

            if (name.isEmpty() || frequencyStr.isEmpty() || stockStr.isEmpty() || schedule.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int frequency = Integer.parseInt(frequencyStr);
            int stock = Integer.parseInt(stockStr);
            int threshold = Integer.parseInt(thresholdStr);

            Medicine medicine = new Medicine(name, type, dosage, frequency, 
                    schedule, 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()), 
                    30, false, stock, threshold);
            
            viewModel.addMedicine(medicine);
            Toast.makeText(this, "Medicine Added", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
