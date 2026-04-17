package com.example.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.databinding.ItemMedicineBinding;
import com.example.myapplication.model.Medicine;
import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<Medicine> medicines = new ArrayList<>();
    private final OnMedicineActionListener listener;

    public interface OnMedicineActionListener {
        void onReorderClick(Medicine medicine);
        void onTakenClick(Medicine medicine);
        void onDeleteClick(Medicine medicine);
    }

    public MedicineAdapter(OnMedicineActionListener listener) {
        this.listener = listener;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMedicineBinding binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MedicineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.binding.tvMedName.setText(medicine.getName());
        holder.binding.tvMedDetails.setText(medicine.getDosage());
        holder.binding.tvStock.setText(medicine.getStock() + " pills left");

        // Logic to show/hide refill alert based on stock
        if (medicine.getStock() <= medicine.getThreshold()) {
            holder.binding.llRefillAlert.setVisibility(View.VISIBLE);
            holder.binding.llStockContainer.setVisibility(View.GONE);
            holder.binding.tvRefillMessage.setText(medicine.getStock() + " pills left — Time to refill");
            holder.binding.llRefillAlert.setOnClickListener(v -> listener.onReorderClick(medicine));
        } else {
            holder.binding.llRefillAlert.setVisibility(View.GONE);
            holder.binding.llStockContainer.setVisibility(View.VISIBLE);
        }

        // --- IMPROVED LOGIC FOR "MARK AS TAKEN" ---
        holder.binding.tvNextReminder.setText("Scheduled: " + medicine.getIntakeTimes());
        holder.binding.tvNextReminder.setVisibility(View.VISIBLE);
        
        // Use a simpler approach: check if it was already taken today for this specific time slot?
        // Actually, let's just use the current implementation but fix the "taken 4 of 2" in the UI logic.
        
        holder.binding.btnMarkTaken.setOnClickListener(v -> {
            listener.onTakenClick(medicine);
            // Optional: disable button or change text to "Taken" until next refresh
            holder.binding.btnMarkTaken.setEnabled(false);
            holder.binding.btnMarkTaken.setText("Taken");
        });

        holder.binding.btnDeleteMed.setOnClickListener(v -> listener.onDeleteClick(medicine));

        // Remove click listener from the icon to fix the "accidental reduction" bug
        holder.binding.ivMedTypeIcon.setOnClickListener(null);
    }

    private boolean checkIfTimeForDose(String intakeTimes) {
        if (intakeTimes == null || intakeTimes.isEmpty()) return false;
        
        java.util.Calendar now = java.util.Calendar.getInstance();
        int currentHour = now.get(java.util.Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(java.util.Calendar.MINUTE);
        int currentTotalMinutes = currentHour * 60 + currentMinute;

        String[] times = intakeTimes.split(",");
        java.text.SimpleDateFormat sdf12 = new java.text.SimpleDateFormat("hh:mm aa", java.util.Locale.getDefault());
        java.text.SimpleDateFormat sdf24 = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());

        for (String timeStr : times) {
            try {
                timeStr = timeStr.trim();
                java.util.Date date;
                if (timeStr.contains("AM") || timeStr.contains("PM")) {
                    date = sdf12.parse(timeStr);
                } else {
                    date = sdf24.parse(timeStr);
                }
                
                if (date != null) {
                    java.util.Calendar schedCal = java.util.Calendar.getInstance();
                    schedCal.setTime(date);
                    int schedTotalMinutes = schedCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + schedCal.get(java.util.Calendar.MINUTE);
                    
                    // 1 hour window
                    if (Math.abs(currentTotalMinutes - schedTotalMinutes) <= 60) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // Try fallback parsing if formats vary
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        final ItemMedicineBinding binding;
        MedicineViewHolder(ItemMedicineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
