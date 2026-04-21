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

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
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
        // Keep local number strict to 10 digits while still storing with country code.
        if (digits.length() != 10) {
            return null;
        }

        String normalized = cleanCountry + digits;
        int normalizedDigits = normalized.replaceAll("[^0-9]", "").length();
        if (normalizedDigits < 10 || normalizedDigits > 15) {
            return null;
        }
        return E164_PATTERN.matcher(normalized).matches() ? normalized : null;
    }

    public static String[] splitCountryCodeAndNumber(String fullPhone) {
        if (fullPhone == null) return new String[]{"+91", ""};

        String trimmed = fullPhone.trim();
        if (!trimmed.startsWith("+")) {
            String digits = trimmed.replaceAll("[^0-9]", "");
            return new String[]{"+91", digits.length() <= 10 ? digits : digits.substring(digits.length() - 10)};
        }

        String digitsOnly = trimmed.replaceAll("[^0-9+]", "");
        String fallbackDigits = digitsOnly.replaceAll("[^0-9]", "");
        String fallbackNumber = fallbackDigits.length() <= 10
                ? fallbackDigits
                : fallbackDigits.substring(fallbackDigits.length() - 10);

        for (int ccLength = 3; ccLength >= 1; ccLength--) {
            int splitIndex = 1 + ccLength;
            if (digitsOnly.length() <= splitIndex) continue;

            String candidateCode = digitsOnly.substring(0, splitIndex);
            String candidateNumber = digitsOnly.substring(splitIndex).replaceAll("[^0-9]", "");
            if (COUNTRY_CODE_PATTERN.matcher(candidateCode).matches() && candidateNumber.length() >= 10) {
                String local = candidateNumber.length() <= 10
                        ? candidateNumber
                        : candidateNumber.substring(candidateNumber.length() - 10);
                return new String[]{candidateCode, local};
            }
        }

        return new String[]{"+91", fallbackNumber};
    }

    public static boolean isValidPassword(String password) {
        // Basic check: at least 6 characters
        return password != null && password.trim().length() >= 6;
    }
}
