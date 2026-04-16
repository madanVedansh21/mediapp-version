package com.example.myapplication.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.databinding.ActivityEmergencyBinding;
import com.example.myapplication.service.MockApiService;

public class EmergencyActivity extends AppCompatActivity {
    private ActivityEmergencyBinding binding;
    private CountDownTimer timer;
    private boolean isCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startCountdown();

        binding.btnCancelEmergency.setOnClickListener(v -> {
            isCancelled = true;
            if (timer != null) timer.cancel();
            Toast.makeText(this, "Emergency Alert Cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startCountdown() {
        timer = new CountDownTimer(10000, 1000) {
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

        MockApiService.sendEmergencyAlert("123 Health St, Metro City", "Aspirin, Lisinopril", new MockApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                com.example.myapplication.util.NotificationHelper.showCustomNotification(
                        EmergencyActivity.this,
                        "Alert Successful",
                        response,
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
}
