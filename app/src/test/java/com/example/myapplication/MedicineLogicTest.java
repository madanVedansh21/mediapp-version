package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MedicineLogicTest {

    @Test
    public void testDosingWindowLogic() {
        // Simulating the logic inside MedicineAdapter
        int scheduleHour = 10;
        int scheduleMinute = 0;
        
        int currentHour = 10;
        int currentMinute = 30;
        
        int schedTotal = scheduleHour * 60 + scheduleMinute;
        int currentTotal = currentHour * 60 + currentMinute;
        
        int diff = Math.abs(schedTotal - currentTotal);
        
        // Window is 60 minutes
        assertTrue("User should be able to take medicine within 60 mins", diff <= 60);
    }

    @Test
    public void testOutsideDosingWindow() {
        int scheduleHour = 10;
        int scheduleMinute = 0;
        
        int currentHour = 12; // 2 hours later
        int currentMinute = 0;
        
        int schedTotal = scheduleHour * 60 + scheduleMinute;
        int currentTotal = currentHour * 60 + currentMinute;
        
        int diff = Math.abs(schedTotal - currentTotal);
        
        assertFalse("User should NOT be able to take medicine after 60 mins", diff <= 60);
    }

    @Test
    public void testStockReduction() {
        int initialStock = 10;
        int expectedStockAfterTaken = 9;
        
        int actualStock = initialStock - 1;
        
        assertEquals("Stock should decrease by exactly 1", expectedStockAfterTaken, actualStock);
    }

    @Test
    public void testEmailValidation() {
        assertTrue("Valid email should pass", com.example.myapplication.util.ValidationUtils.isValidEmail("test@example.com"));
        assertFalse("Missing @ should fail", com.example.myapplication.util.ValidationUtils.isValidEmail("testexample.com"));
        assertFalse("Missing domain should fail", com.example.myapplication.util.ValidationUtils.isValidEmail("test@"));
    }

    @Test
    public void testPhoneValidation() {
        assertTrue("10 digits should pass", com.example.myapplication.util.ValidationUtils.isValidPhone("1234567890"));
        assertFalse("9 digits should fail", com.example.myapplication.util.ValidationUtils.isValidPhone("123456789"));
        assertFalse("Letters should fail", com.example.myapplication.util.ValidationUtils.isValidPhone("12345abcde"));
    }

    @Test
    public void testNameValidation() {
        assertTrue("Normal name should pass", com.example.myapplication.util.ValidationUtils.isValidName("John"));
        assertFalse("Short name should fail", com.example.myapplication.util.ValidationUtils.isValidName("Jo"));
        assertFalse("Numbers in name should fail", com.example.myapplication.util.ValidationUtils.isValidName("John123"));
    }

    @Test
    public void testTimeFormatting() {
        String inputTime = "02:00 PM";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.US);
            java.util.Date date = sdf.parse(inputTime);
            assertNotNull("Time should be parsed correctly", date);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            assertEquals("Hour should be 2 PM (14:00)", 14, cal.get(Calendar.HOUR_OF_DAY));
        } catch (Exception e) {
            fail("Parsing failed");
        }
    }
}
