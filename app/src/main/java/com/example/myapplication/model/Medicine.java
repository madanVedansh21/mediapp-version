package com.example.myapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicines")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String type; // tablet, capsule, liquid
    private String dosage;
    private int frequency; // times per day
    private String intakeTimes; // Comma separated times e.g. "08:00, 20:00"
    private String startDate;
    private int durationDays;
    private boolean isLifetime;
    private int stock;
    private int threshold;
    private String status; // Normal, Pending Reorder

    public Medicine(String name, String type, String dosage, int frequency, String intakeTimes, 
                    String startDate, int durationDays, boolean isLifetime, int stock, int threshold) {
        this.name = name;
        this.type = type;
        this.dosage = dosage;
        this.frequency = frequency;
        this.intakeTimes = intakeTimes;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.isLifetime = isLifetime;
        this.stock = stock;
        this.threshold = threshold;
        this.status = "Normal";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDosage() { return dosage; }
    public int getFrequency() { return frequency; }
    public String getIntakeTimes() { return intakeTimes; }
    public String getStartDate() { return startDate; }
    public int getDurationDays() { return durationDays; }
    public boolean isLifetime() { return isLifetime; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getThreshold() { return threshold; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
