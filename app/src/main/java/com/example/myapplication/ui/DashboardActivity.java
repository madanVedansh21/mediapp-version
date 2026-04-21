package com.example.myapplication.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityDashboardBinding;
import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.model.SymptomLog;
import com.example.myapplication.model.User;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "medibuddy_prefs";
    private static final String KEY_LOGGED_IN_EMAIL = "logged_in_email";
    private ActivityDashboardBinding binding;
    private MediBuddyViewModel viewModel;
    private ActivityResultLauncher<String> pdfExportLauncher;
    private List<IntakeLog> intakeLogsCache = new ArrayList<>();
    private List<SymptomLog> symptomLogsCache = new ArrayList<>();
    private List<Medicine> medicinesCache = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateGreeting();

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);
        pdfExportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"),
            this::writePdfToUri
        );

        String loggedInEmail = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_LOGGED_IN_EMAIL, "");

        if (loggedInEmail == null || loggedInEmail.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        viewModel.getUserByEmail(loggedInEmail).observe(this, user -> {
            if (user != null) {
                currentUser = user;
                binding.tvUserName.setText(user.getName());
                binding.tvCaretakerCount.setText(user.getCaretakerName() != null && !user.getCaretakerName().isEmpty() ? "1" : "0");
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .remove(KEY_LOGGED_IN_EMAIL)
                    .apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        binding.btnAddMedicine.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMedicineActivity.class));
        });

        binding.btnMedicineList.setOnClickListener(v -> {
            startActivity(new Intent(this, MedicineListActivity.class));
        });

        binding.btnSymptomLog.setOnClickListener(v -> {
            startActivity(new Intent(this, SymptomLogActivity.class));
        });

        binding.btnEmergency.setOnClickListener(v -> {
            startActivity(new Intent(this, EmergencyActivity.class));
        });

        binding.btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, LogHistoryActivity.class));
        });

        binding.btnCaretaker.setOnClickListener(v -> {
            startActivity(new Intent(this, CaretakerActivity.class));
        });

        binding.btnExportSummary.setOnClickListener(v -> {
            if (intakeLogsCache.isEmpty() && symptomLogsCache.isEmpty()) {
                Toast.makeText(this, "No summary data available yet", Toast.LENGTH_SHORT).show();
                return;
            }
            String fileName = "MediBuddy_Home_Summary_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".pdf";
            pdfExportLauncher.launch(fileName);
        });

        setupProgressTracker();
        setupAnalytics();
        setupMissedDosesStats();

        viewModel.getEmergencyTriggered().observe(this, triggered -> {
            if (triggered) {
                Intent intent = new Intent(this, EmergencyActivity.class);
                startActivity(intent);
                viewModel.resetEmergencyTrigger();
            }
        });

        viewModel.getAllMedicines().observe(this, medicines -> {
            if (medicines != null) {
                medicinesCache = medicines;
                binding.tvTotalLogs.setText(String.valueOf(medicines.size()));
            }
        });

        viewModel.getAllIntakeLogs().observe(this, logs -> {
            intakeLogsCache = logs != null ? logs : new ArrayList<>();
        });

        viewModel.getAllSymptomLogs().observe(this, logs -> {
            symptomLogsCache = logs != null ? logs : new ArrayList<>();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreeting();
    }

    private void updateGreeting() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning,";
        } else if (hour < 17) {
            greeting = "Good Afternoon,";
        } else if (hour < 21) {
            greeting = "Good Evening,";
        } else {
            greeting = "Good Night,";
        }
        binding.tvGreeting.setText(greeting);
    }

    private void writePdfToUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Export cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        Paint titlePaint = new Paint();
        titlePaint.setColor(0xFF0D47A1);
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);

        Paint sectionPaint = new Paint();
        sectionPaint.setColor(0xFF1A237E);
        sectionPaint.setTextSize(14f);
        sectionPaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint();
        bodyPaint.setColor(0xFF202124);
        bodyPaint.setTextSize(11f);

        Paint cardPaint = new Paint();
        cardPaint.setColor(0xFFEFF6FD);

        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setColor(0xFFD5E5F5);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int margin = 40;
        int y = 48;
        int line = 18;

        canvas.drawRoundRect(30, 24, 565, 90, 16, 16, cardPaint);
        canvas.drawRoundRect(30, 24, 565, 90, 16, 16, borderPaint);
        canvas.drawText("MediBuddy Home Summary", margin, y, titlePaint);
        y += line + 6;
        canvas.drawText("Generated: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()), margin, y, bodyPaint);
        y += line;
        if (currentUser != null) {
            canvas.drawText("User: " + currentUser.getName(), margin, y, bodyPaint);
            y += line;
        }

        int taken = 0;
        int missed = 0;
        for (IntakeLog log : intakeLogsCache) {
            if ("Taken".equalsIgnoreCase(log.getStatus())) {
                taken++;
            } else if ("Missed".equalsIgnoreCase(log.getStatus())) {
                missed++;
            }
        }

        y += line;
        canvas.drawText("Overview", margin, y, sectionPaint);
        y += line;
        canvas.drawText("Medicines tracked: " + medicinesCache.size(), margin, y, bodyPaint);
        y += line;
        canvas.drawText("Intake logs: " + intakeLogsCache.size(), margin, y, bodyPaint);
        y += line;
        canvas.drawText("Taken doses: " + taken, margin, y, bodyPaint);
        y += line;
        canvas.drawText("Missed doses: " + missed, margin, y, bodyPaint);
        y += line;
        canvas.drawText("Symptom logs: " + symptomLogsCache.size(), margin, y, bodyPaint);
        y += line * 2;

        canvas.drawText("Recent Intake", margin, y, sectionPaint);
        y += line;
        SimpleDateFormat intakeSdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        int intakeLimit = Math.min(12, intakeLogsCache.size());
        for (int i = 0; i < intakeLimit; i++) {
            IntakeLog log = intakeLogsCache.get(i);
            canvas.drawText("- " + log.getMedicineName() + " | " + intakeSdf.format(new Date(log.getTimestamp())) + " | " + log.getStatus(), margin, y, bodyPaint);
            y += line;
            if (y > 780) break;
        }

        if (y < 760) {
            y += line;
            canvas.drawText("Recent Symptoms", margin, y, sectionPaint);
            y += line;
            int symptomLimit = Math.min(8, symptomLogsCache.size());
            for (int i = 0; i < symptomLimit; i++) {
                SymptomLog log = symptomLogsCache.get(i);
                canvas.drawText("- " + log.getCategory() + " | Severity " + log.getSeverity(), margin, y, bodyPaint);
                y += line;
                if (y > 780) break;
            }
        }

        List<Medicine> medicinesWithPrescription = new ArrayList<>();
        for (Medicine medicine : medicinesCache) {
            if (medicine.getPrescriptionImageUri() != null && !medicine.getPrescriptionImageUri().trim().isEmpty()) {
                medicinesWithPrescription.add(medicine);
            }
        }

        if (!medicinesWithPrescription.isEmpty() && y < 700) {
            y += line;
            canvas.drawText("Doctor Prescriptions", margin, y, sectionPaint);
            y += line;

            int shown = 0;
            for (Medicine medicine : medicinesWithPrescription) {
                if (shown >= 2 || y > 720) break;
                try {
                    Uri imageUri = Uri.parse(medicine.getPrescriptionImageUri());
                    java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    if (inputStream == null) continue;
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    if (bitmap == null) continue;

                    android.graphics.Rect dest = new android.graphics.Rect(margin, y, margin + 220, y + 120);
                    canvas.drawBitmap(bitmap, null, dest, null);
                    canvas.drawText(medicine.getName(), margin + 230, y + 18, bodyPaint);
                    y += 130;
                    shown++;
                } catch (Exception ignored) {}
            }
        }

        document.finishPage(page);

        try (java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                Toast.makeText(this, "Unable to create PDF", Toast.LENGTH_SHORT).show();
                return;
            }
            document.writeTo(outputStream);
            Toast.makeText(this, "Summary PDF saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private void setupProgressTracker() {
        viewModel.getAllMedicines().observe(this, medicines -> {
            if (medicines == null || medicines.isEmpty()) {
                binding.tvProgressDoses.setText("No medicines scheduled");
                binding.progressIndicator.setProgress(0);
                return;
            }

            int totalExpectedDoses = 0;
            for (Medicine med : medicines) {
                totalExpectedDoses += med.getFrequency();
            }

            long startOfToday = getStartOfDay(0);
            final int finalTotal = totalExpectedDoses;
            
            viewModel.getAllIntakeLogs().observe(this, logs -> {
                int dosesTakenToday = 0;
                if (logs != null) {
                    for (IntakeLog log : logs) {
                        if (log.getTimestamp() >= startOfToday && "Taken".equalsIgnoreCase(log.getStatus())) {
                            dosesTakenToday++;
                        }
                    }
                }

                int displayedTaken = Math.min(dosesTakenToday, finalTotal);
                binding.tvProgressDoses.setText(displayedTaken + " of " + finalTotal + " doses taken Today");
                
                if (finalTotal > 0) {
                    int progress = (int) ((displayedTaken / (float) finalTotal) * 100);
                    binding.progressIndicator.setProgress(progress, true);
                } else {
                    binding.progressIndicator.setProgress(0);
                }
            });
        });
    }

    private void setupAnalytics() {
        viewModel.getAllIntakeLogs().observe(this, logs -> {
            if (logs == null) return;

            ArrayList<BarEntry> takenEntries = new ArrayList<>();
            ArrayList<BarEntry> missedEntries = new ArrayList<>();
            String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            int[] takenCounts = new int[7];
            int[] missedCounts = new int[7];

            Calendar cal = Calendar.getInstance();
            for (IntakeLog log : logs) {
                cal.setTimeInMillis(log.getTimestamp());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                int index = (dayOfWeek + 5) % 7;
                if ("Taken".equalsIgnoreCase(log.getStatus())) {
                    takenCounts[index]++;
                } else if ("Missed".equalsIgnoreCase(log.getStatus())) {
                    missedCounts[index]++;
                }
            }

            for (int i = 0; i < 7; i++) {
                takenEntries.add(new BarEntry(i, takenCounts[i]));
                missedEntries.add(new BarEntry(i, missedCounts[i]));
            }

            BarDataSet takenSet = new BarDataSet(takenEntries, "Taken");
            takenSet.setColor(ContextCompat.getColor(this, R.color.primaryColor));
            takenSet.setValueTextColor(Color.BLACK);

            BarDataSet missedSet = new BarDataSet(missedEntries, "Missed");
            missedSet.setColor(ContextCompat.getColor(this, R.color.accent_red));
            missedSet.setValueTextColor(Color.BLACK);

            BarData barData = new BarData(takenSet, missedSet);
            float groupSpace = 0.06f;
            float barSpace = 0.02f;
            float barWidth = 0.45f;
            barData.setBarWidth(barWidth);

            binding.barChart.setData(barData);
            binding.barChart.groupBars(0, groupSpace, barSpace);
            
            XAxis xAxis = binding.barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f);
            xAxis.setAxisMinimum(0);
            xAxis.setAxisMaximum(7);
            
            binding.barChart.getAxisLeft().setAxisMinimum(0f);
            binding.barChart.getAxisRight().setEnabled(false);
            binding.barChart.getDescription().setEnabled(false);
            binding.barChart.animateY(1000);
            binding.barChart.invalidate();
        });
    }

    private void setupMissedDosesStats() {
        viewModel.getAllIntakeLogs().observe(this, logs -> {
            int missedToday = 0;
            long startOfToday = getStartOfDay(0);
            if (logs != null) {
                for (IntakeLog log : logs) {
                    if (log.getTimestamp() >= startOfToday && "Missed".equalsIgnoreCase(log.getStatus())) {
                        missedToday++;
                    }
                }
            }
            binding.tvCaretakerCount.setText(String.valueOf(missedToday));
            binding.tvCaretakerLabel.setText("Missed Today");
        });
    }

    private long getStartOfDay(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
