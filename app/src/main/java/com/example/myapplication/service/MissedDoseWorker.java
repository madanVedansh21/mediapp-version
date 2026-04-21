package com.example.myapplication.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MissedDoseWorker extends Worker {

    public MissedDoseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        List<Medicine> medicines = db.medicineDao().getAllMedicinesSync();
        
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        
        for (Medicine med : medicines) {
            String[] times = med.getIntakeTimes().split(",");
            for (String timeStr : times) {
                long scheduledTime = parseTimeToTodayMillis(timeStr.trim());
                // If scheduled time + 60 mins has passed and it's still today
                if (now > scheduledTime + 3600000) {
                    // Check if already logged for this window
                    List<IntakeLog> logs = db.intakeLogDao().getLogsForMedicineSince(med.getId(), scheduledTime - 3600000);
                    boolean recorded = false;
                    for (IntakeLog log : logs) {
                        if (Math.abs(log.getTimestamp() - scheduledTime) < 3600000) {
                            recorded = true;
                            break;
                        }
                    }
                    
                    if (!recorded) {
                        db.intakeLogDao().insert(new IntakeLog(med.getId(), med.getName(), scheduledTime, "Missed"));
                    }
                }
            }
        }
        
        return Result.success();
    }

    private long parseTimeToTodayMillis(String timeStr) {
        String[] formats = {"hh:mm a", "h:mm a", "hh:mm aa", "h:mm aa", "HH:mm", "H:mm"};
        Date date = null;
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                date = sdf.parse(timeStr);
                if (date != null) break;
            } catch (Exception ignored) {}
        }

        if (date == null) return 0;

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(date);
        
        Calendar schedCal = Calendar.getInstance();
        schedCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        schedCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        schedCal.set(Calendar.SECOND, 0);
        schedCal.set(Calendar.MILLISECOND, 0);
        
        return schedCal.getTimeInMillis();
    }
}
