package com.example.myapplication.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.model.SymptomLog;
import com.example.myapplication.model.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediBuddyRepository {
    private final UserDao userDao;
    private final MedicineDao medicineDao;
    private final IntakeLogDao intakeLogDao;
    private final SymptomLogDao symptomLogDao;
    private final ExecutorService executorService;

    public MediBuddyRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        medicineDao = db.medicineDao();
        intakeLogDao = db.intakeLogDao();
        symptomLogDao = db.symptomLogDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // User operations
    public void register(User user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public User login(String email, String password) {
        // This is a simplified login for this exercise
        return userDao.login(email, password);
    }

    public LiveData<User> getUser() {
        return userDao.getUser();
    }

    public void updateUser(User user) {
        executorService.execute(() -> userDao.update(user));
    }

    // Medicine operations
    public void insertMedicine(Medicine medicine) {
        executorService.execute(() -> medicineDao.insert(medicine));
    }

    public void updateMedicine(Medicine medicine) {
        executorService.execute(() -> medicineDao.update(medicine));
    }

    public void deleteMedicine(Medicine medicine) {
        executorService.execute(() -> medicineDao.delete(medicine));
    }

    public LiveData<List<Medicine>> getAllMedicines() {
        return medicineDao.getAllMedicines();
    }

    public LiveData<List<Medicine>> getLowStockMedicines() {
        return medicineDao.getLowStockMedicines();
    }

    // Intake Log operations
    public void logIntake(IntakeLog log) {
        executorService.execute(() -> {
            intakeLogDao.insert(log);
            if ("Taken".equals(log.getStatus())) {
                // Logic to reduce stock would be triggered here in a real app
            }
        });
    }

    public LiveData<List<IntakeLog>> getAllIntakeLogs() {
        return intakeLogDao.getAllLogs();
    }

    // Symptom Log operations
    public void logSymptom(SymptomLog log) {
        executorService.execute(() -> symptomLogDao.insert(log));
    }

    public LiveData<List<SymptomLog>> getAllSymptomLogs() {
        return symptomLogDao.getAllLogs();
    }
    
    public List<SymptomLog> getRecentSymptomsSync(long since) {
        return symptomLogDao.getRecentLogsSync(since);
    }
}
