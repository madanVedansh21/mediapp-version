package com.example.myapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String email;
    private String password;
    private String emergencyContact;
    private String caretakerName;
    private String caretakerPhone;
    private String caretakerEmail;
    private String hospital;
    private String hospitalPhone;
    private String doctorName;
    private String doctorPhone;
    private String doctorEmail;

    public User(String name, String email, String password, String emergencyContact, String caretakerName, String caretakerPhone, String caretakerEmail, String hospital, String hospitalPhone, String doctorName, String doctorPhone, String doctorEmail) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.emergencyContact = emergencyContact;
        this.caretakerName = caretakerName;
        this.caretakerPhone = caretakerPhone;
        this.caretakerEmail = caretakerEmail;
        this.hospital = hospital;
        this.hospitalPhone = hospitalPhone;
        this.doctorName = doctorName;
        this.doctorPhone = doctorPhone;
        this.doctorEmail = doctorEmail;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getCaretakerName() { return caretakerName; }
    public String getCaretakerPhone() { return caretakerPhone; }
    public String getCaretakerEmail() { return caretakerEmail; }
    public String getHospital() { return hospital; }
    public String getHospitalPhone() { return hospitalPhone; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorPhone() { return doctorPhone; }
    public String getDoctorEmail() { return doctorEmail; }
}
