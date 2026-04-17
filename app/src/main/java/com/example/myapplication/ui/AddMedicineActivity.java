package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityAddMedicineBinding;
import com.example.myapplication.model.Medicine;
import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {
    private ActivityAddMedicineBinding binding;
    private MediBuddyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        binding.etSchedule.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            android.app.TimePickerDialog mTimePicker;
            mTimePicker = new android.app.TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                String am_pm = (selectedHour < 12) ? "AM" : "PM";
                int hour12 = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
                binding.etSchedule.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d %s", hour12, selectedMinute, am_pm));
            }, hour, minute, false); // 'false' for 12-hour view
            mTimePicker.setTitle("Select Intake Time");
            mTimePicker.show();
        });

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
