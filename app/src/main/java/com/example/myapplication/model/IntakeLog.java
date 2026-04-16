package com.example.myapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "intake_logs")
public class IntakeLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int medicineId;
    private String medicineName;
    private long timestamp;
    private String status; // "Taken", "Missed"

    public IntakeLog(int medicineId, String medicineName, long timestamp, String status) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.timestamp = timestamp;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}
