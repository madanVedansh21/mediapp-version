package com.example.myapplication.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityEmergencyBinding;
import com.example.myapplication.service.MockApiService;
import com.example.myapplication.model.User;
import androidx.lifecycle.Observer;

public class EmergencyActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 101;
    private ActivityEmergencyBinding binding;
    private MediBuddyViewModel viewModel;
    private CountDownTimer timer;
    private boolean isCancelled = false;
    private String caretakerPhone = "+918735047450"; // Fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MediBuddyViewModel.class);
        startCountdown();

        binding.btnCancelEmergency.setOnClickListener(v -> {
            isCancelled = true;
            if (timer != null) timer.cancel();
            Toast.makeText(this, "Emergency Alert Cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startCountdown() {
        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (!isCancelled) {
                    sendAlert();
                }
            }
        }.start();
    }

    private void sendAlert() {
        binding.tvCountdown.setText("!");
        binding.tvEmergencyStatus.setText("ALERT SENT");
        binding.btnCancelEmergency.setVisibility(View.GONE);

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            sendSmsToCaretaker();
        }

        MockApiService.sendEmergencyAlert("Location: near IIIT Nagpur , Nagpur Rural", "Aspirin, Lisinopril", new MockApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                // Also simulate sending to caretaker
                String caretakerMsg = "SOS! Emergency alert sent to your caretaker and City Hospital.";
                
                com.example.myapplication.util.NotificationHelper.showCustomNotification(
                        EmergencyActivity.this,
                        "Emergency Triggered",
                        caretakerMsg,
                        android.R.drawable.stat_sys_warning,
                        R.color.accent_red);
            }

            @Override
            public void onError(String error) {
                com.example.myapplication.util.NotificationHelper.showCustomNotification(
                        EmergencyActivity.this,
                        "Alert Failed",
                        error,
                        android.R.drawable.ic_dialog_alert,
                        R.color.accent_red);
            }
        });
    }

    private void sendSmsToCaretaker() {
        viewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null && user.getCaretakerPhone() != null && !user.getCaretakerPhone().isEmpty()) {
                    caretakerPhone = user.getCaretakerPhone();
                }
                performSmsSend();
                viewModel.getUser().removeObserver(this);
            }
        });
    }

    private void performSmsSend() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "EMERGENCY! MediBuddy user needs help. Location: near IIIT Nagpur , Nagpur Rural ";
            smsManager.sendTextMessage(caretakerPhone, null, message, null, null);
            Toast.makeText(EmergencyActivity.this, "Emergency SMS Sent to " + caretakerPhone, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(EmergencyActivity.this, "SMS failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSmsToCaretaker();
            } else {
                Toast.makeText(this, "SMS Permission Denied. Alert not sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
