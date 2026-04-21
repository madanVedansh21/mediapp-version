package com.example.myapplication.ui;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityAddMedicineBinding;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.util.AlarmUtils;
import com.example.myapplication.util.ValidationUtils;
import android.os.Build;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddMedicineActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "medibuddy_prefs";
    private static final String KEY_LOGGED_IN_EMAIL = "logged_in_email";
    private ActivityAddMedicineBinding binding;
    private MediBuddyViewModel viewModel;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private ActivityResultLauncher<String[]> prescriptionPickerLauncher;
    private Uri selectedPrescriptionUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);
        prescriptionPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        selectedPrescriptionUri = uri;
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception ignored) {}

                        binding.ivPrescriptionPreview.setImageURI(uri);
                        binding.ivPrescriptionPreview.setVisibility(android.view.View.VISIBLE);
                        binding.btnPickPrescription.setText("Change Prescription Image");
                    }
                }
        );

        checkNotificationPermission();

        binding.btnPickPrescription.setOnClickListener(v ->
            prescriptionPickerLauncher.launch(new String[]{"image/*"})
        );

        binding.etSchedule.setOnClickListener(v -> {
            String freqStr = binding.etFrequency.getText().toString();
            if (freqStr.isEmpty()) {
                Toast.makeText(this, "Please enter frequency first", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int frequency = Integer.parseInt(freqStr);
                if (frequency <= 0) {
                    Toast.makeText(this, "Frequency must be at least 1", Toast.LENGTH_SHORT).show();
                    return;
                }
                pickTimes(frequency, 1, "");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid frequency", Toast.LENGTH_SHORT).show();
            }
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

            if (!ValidationUtils.isValidMedicineName(name)) {
                Toast.makeText(this, "Medicine name must be 3-20 alphabetic characters", Toast.LENGTH_SHORT).show();
                return;
            }

            int frequency = Integer.parseInt(frequencyStr);
            int stock = Integer.parseInt(stockStr);
            int threshold = Integer.parseInt(thresholdStr);

            Medicine medicine = new Medicine(name, type, dosage, frequency, 
                    schedule, 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()), 
                    30, false, stock, threshold);

                String loggedInEmail = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString(KEY_LOGGED_IN_EMAIL, "");
                medicine.setOwnerEmail(loggedInEmail == null ? "" : loggedInEmail);
                medicine.setPrescriptionImageUri(selectedPrescriptionUri != null ? selectedPrescriptionUri.toString() : "");
            
            viewModel.addMedicine(medicine);
            AlarmUtils.scheduleNotifications(this, medicine);
            Toast.makeText(this, "Medicine Added & Reminders Set", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void pickTimes(int total, int current, String accumulatedTimes) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        
        android.app.TimePickerDialog mTimePicker;
        mTimePicker = new android.app.TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            String am_pm = (selectedHour < 12) ? "AM" : "PM";
            int hour12 = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
            String selectedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", hour12, selectedMinute, am_pm);
            
            String newAccumulated = accumulatedTimes.isEmpty() ? selectedTime : accumulatedTimes + ", " + selectedTime;
            
            if (current < total) {
                pickTimes(total, current + 1, newAccumulated);
            } else {
                binding.etSchedule.setText(newAccumulated);
            }
        }, hour, minute, false);
        
        mTimePicker.setTitle("Select Time for Dose " + current + " of " + total);
        mTimePicker.show();
    }

}
