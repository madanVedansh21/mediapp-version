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
    private String hospital;

    public User(String name, String email, String password, String emergencyContact, String hospital) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.emergencyContact = emergencyContact;
        this.hospital = hospital;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getHospital() { return hospital; }
}
