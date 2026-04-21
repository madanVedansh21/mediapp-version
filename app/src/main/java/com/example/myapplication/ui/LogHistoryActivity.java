package com.example.myapplication.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityLogHistoryBinding;
import com.example.myapplication.model.IntakeLog;
import com.example.myapplication.model.Medicine;
import com.example.myapplication.model.SymptomLog;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogHistoryActivity extends AppCompatActivity {
    private ActivityLogHistoryBinding binding;
    private MediBuddyViewModel viewModel;
    private LogAdapter adapter;
    private List<IntakeLog> intakeLogsCache = new ArrayList<>();
    private List<Medicine> medicinesCache = new ArrayList<>();
    private List<SymptomLog> symptomLogsCache = new ArrayList<>();
    private int selectedTab = 0;
    private ActivityResultLauncher<String> pdfExportLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);
        adapter = new LogAdapter();
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLogs.setAdapter(adapter);

        pdfExportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                this::writePdfToUri
        );

        binding.btnDownloadSummary.setOnClickListener(v -> {
            if (intakeLogsCache.isEmpty() && symptomLogsCache.isEmpty()) {
                Toast.makeText(this, "No data available for export", Toast.LENGTH_SHORT).show();
                return;
            }
            String fileName = "MediBuddy_Summary_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".pdf";
            pdfExportLauncher.launch(fileName);
        });

        viewModel.getAllIntakeLogs().observe(this, logs -> {
            intakeLogsCache = logs != null ? logs : new ArrayList<>();
            renderCurrentTab();
        });

        viewModel.getAllMedicines().observe(this, medicines -> {
            medicinesCache = medicines != null ? medicines : new ArrayList<>();
        });

        viewModel.getAllSymptomLogs().observe(this, logs -> {
            symptomLogsCache = logs != null ? logs : new ArrayList<>();
            renderCurrentTab();
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                renderCurrentTab();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Default view
        renderCurrentTab();
    }

    private void renderCurrentTab() {
        List<LogDisplayItem> displayLogs = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

        if (selectedTab == 0) {
            for (IntakeLog log : intakeLogsCache) {
                displayLogs.add(new LogDisplayItem(
                        log.getMedicineName(),
                        sdf.format(new Date(log.getTimestamp())),
                        log.getStatus()
                ));
            }
        } else {
            for (SymptomLog log : symptomLogsCache) {
                displayLogs.add(new LogDisplayItem(
                        log.getCategory(),
                        log.getNotes() + " (" + sdf.format(new Date(log.getTimestamp())) + ")",
                        "Level " + log.getSeverity()
                ));
            }
        }

        adapter.setData(displayLogs);
    }

    private void writePdfToUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "PDF export cancelled", Toast.LENGTH_SHORT).show();
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
        bodyPaint.setColor(0xFF222222);
        bodyPaint.setTextSize(11f);

        Paint cardPaint = new Paint();
        cardPaint.setColor(0xFFEFF6FD);

        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setColor(0xFFD5E5F5);

        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int marginLeft = 40;
        int y = 48;
        int lineHeight = 18;
        int maxY = 800;

        canvas.drawRoundRect(30, 24, 565, 90, 16, 16, cardPaint);
        canvas.drawRoundRect(30, 24, 565, 90, 16, 16, borderPaint);
        canvas.drawText("MediBuddy Health Summary", marginLeft, y, titlePaint);
        y += lineHeight + 6;
        canvas.drawText("Generated: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()), marginLeft, y, bodyPaint);
        y += lineHeight * 2;

        int takenCount = 0;
        int missedCount = 0;
        for (IntakeLog log : intakeLogsCache) {
            if ("Taken".equalsIgnoreCase(log.getStatus())) {
                takenCount++;
            } else if ("Missed".equalsIgnoreCase(log.getStatus())) {
                missedCount++;
            }
        }

        canvas.drawText("Summary", marginLeft, y, sectionPaint);
        y += lineHeight;
        canvas.drawText("Total medicine logs: " + intakeLogsCache.size(), marginLeft, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Taken doses: " + takenCount, marginLeft, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Missed doses: " + missedCount, marginLeft, y, bodyPaint);
        y += lineHeight;
        canvas.drawText("Total symptom logs: " + symptomLogsCache.size(), marginLeft, y, bodyPaint);
        y += lineHeight * 2;

        canvas.drawText("Medicine History", marginLeft, y, sectionPaint);
        y += lineHeight;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        int intakeLimit = Math.min(intakeLogsCache.size(), 40);
        for (int i = 0; i < intakeLimit; i++) {
            IntakeLog log = intakeLogsCache.get(i);
            if (y > maxY) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 48;
                canvas.drawText("Medicine History (continued)", marginLeft, y, sectionPaint);
                y += lineHeight;
            }

            String line = "- " + log.getMedicineName() + " | " + sdf.format(new Date(log.getTimestamp())) + " | " + log.getStatus();
            canvas.drawText(line, marginLeft, y, bodyPaint);
            y += lineHeight;
        }

        y += lineHeight;
        if (y > maxY) {
            document.finishPage(page);
            pageNumber++;
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 48;
        }

        canvas.drawText("Symptom History", marginLeft, y, sectionPaint);
        y += lineHeight;

        int symptomLimit = Math.min(symptomLogsCache.size(), 40);
        for (int i = 0; i < symptomLimit; i++) {
            SymptomLog log = symptomLogsCache.get(i);
            if (y > maxY) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 48;
                canvas.drawText("Symptom History (continued)", marginLeft, y, sectionPaint);
                y += lineHeight;
            }

            String line = "- " + log.getCategory() + " | Severity " + log.getSeverity() + " | " + sdf.format(new Date(log.getTimestamp()));
            canvas.drawText(line, marginLeft, y, bodyPaint);
            y += lineHeight;
            if (log.getNotes() != null && !log.getNotes().trim().isEmpty()) {
                String notesLine = "  Notes: " + log.getNotes().trim();
                canvas.drawText(notesLine, marginLeft, y, bodyPaint);
                y += lineHeight;
            }
        }

        List<Medicine> medicinesWithPrescription = new ArrayList<>();
        for (Medicine medicine : medicinesCache) {
            if (medicine.getPrescriptionImageUri() != null && !medicine.getPrescriptionImageUri().trim().isEmpty()) {
                medicinesWithPrescription.add(medicine);
            }
        }

        if (!medicinesWithPrescription.isEmpty()) {
            if (y > maxY - 140) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 48;
            }

            canvas.drawText("Prescription Images", marginLeft, y, sectionPaint);
            y += lineHeight;

            int shown = 0;
            for (Medicine medicine : medicinesWithPrescription) {
                if (shown >= 4) break;
                if (y > maxY - 130) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 48;
                }

                try {
                    Uri imageUri = Uri.parse(medicine.getPrescriptionImageUri());
                    java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    if (inputStream == null) continue;
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    if (bitmap == null) continue;

                    android.graphics.Rect dest = new android.graphics.Rect(marginLeft, y, marginLeft + 220, y + 120);
                    canvas.drawBitmap(bitmap, null, dest, null);
                    canvas.drawText(medicine.getName(), marginLeft + 230, y + 18, bodyPaint);
                    y += 130;
                    shown++;
                } catch (Exception ignored) {}
            }
        }

        document.finishPage(page);

        try (java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                Toast.makeText(this, "Could not create PDF file", Toast.LENGTH_SHORT).show();
                return;
            }
            document.writeTo(outputStream);
            Toast.makeText(this, "PDF summary saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "PDF export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    static class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
        private List<LogDisplayItem> data = new ArrayList<>();

        public void setData(List<LogDisplayItem> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LogDisplayItem item = data.get(position);
            holder.title.setText(item.title);
            holder.subtitle.setText(item.subtitle);
            holder.status.setText(item.status);
            
            if ("Missed".equalsIgnoreCase(item.status)) {
                holder.status.setTextColor(0xFFC62828);
                holder.status.setBackgroundColor(0xFFFFEBEE);
            } else if (item.status != null && item.status.startsWith("Level")) {
                holder.status.setTextColor(0xFF6A1B9A);
                holder.status.setBackgroundColor(0xFFF3E5F5);
            } else {
                holder.status.setTextColor(0xFF2E7D32);
                holder.status.setBackgroundColor(0xFFE8F5E9);
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, subtitle, status;
            ViewHolder(View itemView) { 
                super(itemView);
                title = itemView.findViewById(R.id.tvLogTitle);
                subtitle = itemView.findViewById(R.id.tvLogSubtitle);
                status = itemView.findViewById(R.id.tvLogStatus);
            }
        }
    }

    static class LogDisplayItem {
        String title, subtitle, status;
        LogDisplayItem(String title, String subtitle, String status) {
            this.title = title;
            this.subtitle = subtitle;
            this.status = status;
        }
    }
}
