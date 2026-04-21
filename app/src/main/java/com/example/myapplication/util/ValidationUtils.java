package com.example.myapplication.util;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex: Only alphabetic characters, 3-20 length
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]{3,20}$");
    
    // Regex: Exactly 10 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^\\+[1-9][0-9]{0,2}$");
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9][0-9]{9,14}$");

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

    public static String normalizePhoneWithCountryCode(String countryCode, String phone) {
        if (countryCode == null || phone == null) return null;

        String cleanCountry = countryCode.trim();
        if (!COUNTRY_CODE_PATTERN.matcher(cleanCountry).matches()) {
            return null;
        }

        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 10 || digits.length() > 14) {
            return null;
        }

        String normalized = cleanCountry + digits;
        return E164_PATTERN.matcher(normalized).matches() ? normalized : null;
    }

    public static String[] splitCountryCodeAndNumber(String fullPhone) {
        if (fullPhone == null) return new String[]{"+91", ""};

        String trimmed = fullPhone.trim();
        if (!trimmed.startsWith("+")) {
            return new String[]{"+91", trimmed.replaceAll("[^0-9]", "")};
        }

        String digitsOnly = trimmed.replaceAll("[^0-9+]", "");
        for (int ccLength = 3; ccLength >= 1; ccLength--) {
            int splitIndex = 1 + ccLength;
            if (digitsOnly.length() <= splitIndex) continue;

            String candidateCode = digitsOnly.substring(0, splitIndex);
            String candidateNumber = digitsOnly.substring(splitIndex).replaceAll("[^0-9]", "");
            if (COUNTRY_CODE_PATTERN.matcher(candidateCode).matches() && candidateNumber.length() >= 10) {
                return new String[]{candidateCode, candidateNumber};
            }
        }

        return new String[]{"+91", digitsOnly.replaceAll("[^0-9]", "")};
    }

    public static boolean isValidPassword(String password) {
        // Basic check: at least 6 characters
        return password != null && password.trim().length() >= 6;
    }
}
