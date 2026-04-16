package com.example.myapplication.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.databinding.LayoutCustomNotificationBinding;

public class NotificationHelper {

    public static void showCustomNotification(Activity activity, String title, String message, int iconResId, int iconColor) {
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        LayoutCustomNotificationBinding binding = LayoutCustomNotificationBinding.inflate(
                LayoutInflater.from(activity), rootView, false);

        binding.tvNotificationTitle.setText(title);
        binding.tvNotificationMessage.setText(message);
        binding.ivNotificationIcon.setImageResource(iconResId);
        binding.ivNotificationIcon.setColorFilter(activity.getResources().getColor(iconColor));

        rootView.addView(binding.getRoot());

        // Initial state: Off-screen top
        binding.getRoot().setTranslationY(-300f);
        binding.getRoot().setAlpha(0f);

        // Animate In
        binding.getRoot().animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Close logic
        Runnable closeRunnable = () -> {
            binding.getRoot().animate()
                    .translationY(-300f)
                    .alpha(0f)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> rootView.removeView(binding.getRoot()))
                    .start();
        };

        binding.ivClose.setOnClickListener(v -> closeRunnable.run());

        // Auto-close after 4 seconds
        binding.getRoot().postDelayed(closeRunnable, 4000);
    }
}
