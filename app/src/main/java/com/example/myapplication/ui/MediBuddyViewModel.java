package com.example.myapplication.ui;

import android.app.Application;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.MediBuddyRepository;
import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.model.SymptomLog;
import com.example.myapplication.model.User;
import com.example.myapplication.service.MockApiService;

import java.util.List;

public class MediBuddyViewModel extends AndroidViewModel {
    private final MediBuddyRepository repository;
    private final MutableLiveData<Boolean> emergencyTriggered = new MutableLiveData<>(false);

    public MediBuddyViewModel(@NonNull Application application) {
        super(application);
        repository = new MediBuddyRepository(application);
    }

    public LiveData<Boolean> getEmergencyTriggered() {
        return emergencyTriggered;
    }

    public void resetEmergencyTrigger() {
        emergencyTriggered.postValue(false);
    }

    // User
    public void register(User user) { repository.register(user); }
    public void updateUser(User user) { repository.updateUser(user); }
    public LiveData<User> getUser() { return repository.getUser(); }

    // Medicine
    public void addMedicine(Medicine medicine) { repository.insertMedicine(medicine); }
    public void updateMedicine(Medicine medicine) { repository.updateMedicine(medicine); }
    public void deleteMedicine(Medicine medicine) { repository.deleteMedicine(medicine); }
    public LiveData<List<Medicine>> getAllMedicines() { return repository.getAllMedicines(); }
    public LiveData<List<Medicine>> getLowStockMedicines() { return repository.getLowStockMedicines(); }

    // Intake
    public void logIntake(IntakeLog log) { repository.logIntake(log); }
    public LiveData<List<IntakeLog>> getAllIntakeLogs() { return repository.getAllIntakeLogs(); }

    // Symptoms
    public void logSymptom(SymptomLog log) { repository.logSymptom(log); }
    public LiveData<List<SymptomLog>> getAllSymptomLogs() { return repository.getAllSymptomLogs(); }

    public void checkSymptomSeverity() {
        new Thread(() -> {
            long fortyEightHoursAgo = System.currentTimeMillis() - (48 * 60 * 60 * 1000);
            List<SymptomLog> recent = repository.getRecentSymptomsSync(fortyEightHoursAgo);
            int highSeverityCount = 0;
            for (SymptomLog s : recent) {
                if (s.getSeverity() >= 7) highSeverityCount++;
            }
            if (highSeverityCount >= 3) {
                emergencyTriggered.postValue(true);
            }
        }).start();
    }
    
    public void reorderMedicine(Medicine medicine) {
        MockApiService.placeMedicineOrder(medicine.getName(), new MockApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                medicine.setStock(medicine.getStock() + 50); // Simulate receiving stock
                medicine.setStatus("Normal");
                updateMedicine(medicine);
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }
}
