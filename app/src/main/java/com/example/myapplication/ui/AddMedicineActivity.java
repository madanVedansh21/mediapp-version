package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityAddMedicineBinding;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.receiver.MedicineAlarmReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.os.Build;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddMedicineActivity extends AppCompatActivity {
    private ActivityAddMedicineBinding binding;
    private MediBuddyViewModel viewModel;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        checkNotificationPermission();

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

            int frequency = Integer.parseInt(frequencyStr);
            int stock = Integer.parseInt(stockStr);
            int threshold = Integer.parseInt(thresholdStr);

            Medicine medicine = new Medicine(name, type, dosage, frequency, 
                    schedule, 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()), 
                    30, false, stock, threshold);
            
            viewModel.addMedicine(medicine);
            scheduleNotifications(medicine);
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

    private void scheduleNotifications(Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String[] times = medicine.getIntakeTimes().split(",");
        String[] formats = {"hh:mm a", "h:mm a", "hh:mm aa", "h:mm aa", "HH:mm", "H:mm"};

        for (int i = 0; i < times.length; i++) {
            String timeStr = times[i].trim();
            Date date = null;
            for (String format : formats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                    date = sdf.parse(timeStr);
                    if (date != null) break;
                } catch (Exception ignored) {}
            }

            if (date != null) {
                Calendar schedCal = Calendar.getInstance();
                Calendar now = Calendar.getInstance();
                
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(date);
                
                schedCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                schedCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                schedCal.set(Calendar.SECOND, 0);

                if (schedCal.before(now)) {
                    schedCal.add(Calendar.DAY_OF_YEAR, 1);
                }

                Intent intent = new Intent(this, MedicineAlarmReceiver.class);
                intent.putExtra("medicine_name", medicine.getName());
                intent.putExtra("medicine_id", medicine.getId());
                
                // Use a unique request code for each dose
                int requestCode = (medicine.getName().hashCode() + i);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, schedCal.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, schedCal.getTimeInMillis(), pendingIntent);
                }
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
