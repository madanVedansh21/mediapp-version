package com.example.myapplication;

import android.app.Application;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.myapplication.service.MissedDoseWorker;
import java.util.concurrent.TimeUnit;

public class MediBuddyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        scheduleMissedDoseTracking();
    }

    private void scheduleMissedDoseTracking() {
        PeriodicWorkRequest missedDoseWorkRequest =
                new PeriodicWorkRequest.Builder(MissedDoseWorker.class, 6, TimeUnit.HOURS)
                        .addTag("MissedDoseWork")
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MissedDoseWork",
                ExistingPeriodicWorkPolicy.KEEP,
                missedDoseWorkRequest
        );
    }
}
