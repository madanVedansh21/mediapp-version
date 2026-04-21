package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.MedicineDao;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.util.AlarmUtils;
import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Executors.newSingleThreadExecutor().execute(() -> {
                MedicineDao medicineDao = AppDatabase.getDatabase(context).medicineDao();
                List<Medicine> medicines = medicineDao.getAllMedicinesSync();
                if (medicines != null) {
                    for (Medicine medicine : medicines) {
                        AlarmUtils.scheduleNotifications(context, medicine);
                    }
                }
            });
        }
    }
}
