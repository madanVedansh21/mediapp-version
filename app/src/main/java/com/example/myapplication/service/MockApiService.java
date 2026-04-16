package com.example.myapplication.service;

import android.util.Log;

public class MockApiService {
    private static final String TAG = "MockApiService";

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void placeMedicineOrder(String medicineName, ApiCallback callback) {
        // Simulate POST /mock/pharmacy/order
        new android.os.Handler().postDelayed(() -> {
            Log.d(TAG, "Ordering medicine: " + medicineName);
            callback.onSuccess("Order placed successfully for " + medicineName);
        }, 1500);
    }

    public static void sendEmergencyAlert(String location, String medicineList, ApiCallback callback) {
        // Simulate POST /mock/emergency/alert
        new android.os.Handler().postDelayed(() -> {
            Log.d(TAG, "Emergency alert sent. Location: " + location + ", Meds: " + medicineList);
            callback.onSuccess("Emergency alert sent");
        }, 2000);
    }

    public static void sendEmail(String to, String subject, String body) {
        // Simulate Email
        Log.d(TAG, "Email Sent To: " + to + "\nSubject: " + subject + "\nBody: " + body);
    }

    public static void sendSMS(String phoneNumber, String message) {
        // Simulate SMS
        Log.d(TAG, "SMS Sent To: " + phoneNumber + "\nMessage: " + message);
    }
}
