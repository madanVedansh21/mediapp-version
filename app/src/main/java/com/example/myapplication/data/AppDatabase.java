package com.example.myapplication.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.model.SymptomLog;
import com.example.myapplication.model.User;

@Database(entities = {User.class, Medicine.class, IntakeLog.class, SymptomLog.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract MedicineDao medicineDao();
    public abstract IntakeLogDao intakeLogDao();
    public abstract SymptomLogDao symptomLogDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "medibuddy_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
