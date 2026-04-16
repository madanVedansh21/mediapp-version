package com.example.myapplication.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.Medicine;

import java.util.List;

@Dao
public interface MedicineDao {
    @Insert
    void insert(Medicine medicine);

    @Update
    void update(Medicine medicine);

    @Query("SELECT * FROM medicines")
    LiveData<List<Medicine>> getAllMedicines();

    @Query("SELECT * FROM medicines WHERE stock <= threshold")
    LiveData<List<Medicine>> getLowStockMedicines();
}
