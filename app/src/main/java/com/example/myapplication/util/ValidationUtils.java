package com.example.myapplication.util;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex: Only alphabetic characters, 3-20 length
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]{3,20}$");
    
    // Regex: Exactly 10 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim();
    }

    public static boolean isValidName(String name) {
        if (name == null) return false;
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidMedicineName(String name) {
        if (name == null) return false;
        // Allows letters and spaces, 3-30 characters
        return name.trim().matches("^[a-zA-Z\\s]{3,30}$");
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(trimmed).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        // Basic check: at least 6 characters
        return password != null && password.trim().length() >= 6;
    }
}
