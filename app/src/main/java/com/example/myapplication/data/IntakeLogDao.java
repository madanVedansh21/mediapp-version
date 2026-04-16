package com.example.myapplication.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.model.IntakeLog;

import java.util.List;

@Dao
public interface IntakeLogDao {
    @Insert
    void insert(IntakeLog log);

    @Query("SELECT * FROM intake_logs ORDER BY timestamp DESC")
    LiveData<List<IntakeLog>> getAllLogs();

    @Query("SELECT * FROM intake_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    LiveData<List<IntakeLog>> getRecentLogs(long since);
}
