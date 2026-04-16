package com.example.myapplication.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.model.SymptomLog;

import java.util.List;

@Dao
public interface SymptomLogDao {
    @Insert
    void insert(SymptomLog log);

    @Query("SELECT * FROM symptom_logs ORDER BY timestamp DESC")
    LiveData<List<SymptomLog>> getAllLogs();

    @Query("SELECT * FROM symptom_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    List<SymptomLog> getRecentLogsSync(long since);
}
