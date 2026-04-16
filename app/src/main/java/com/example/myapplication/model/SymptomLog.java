package com.example.myapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "symptom_logs")
public class SymptomLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String category; // pain, digestion, mood, etc.
    private int severity; // 1-10
    private String notes;
    private long timestamp;

    public SymptomLog(String category, int severity, String notes, long timestamp) {
        this.category = category;
        this.severity = severity;
        this.notes = notes;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCategory() { return category; }
    public int getSeverity() { return severity; }
    public String getNotes() { return notes; }
    public long getTimestamp() { return timestamp; }
}
