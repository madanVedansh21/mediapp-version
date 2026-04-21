package com.example.myapplication.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.receiver.MedicineAlarmReceiver;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmUtils {

    public static void scheduleNotifications(Context context, Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

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

                Intent intent = new Intent(context, MedicineAlarmReceiver.class);
                intent.putExtra("medicine_name", medicine.getName());
                intent.putExtra("medicine_id", medicine.getId());
                
                int requestCode = (medicine.getName().hashCode() + i);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, schedCal.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, schedCal.getTimeInMillis(), pendingIntent);
                }
            }
        }
    }
}
