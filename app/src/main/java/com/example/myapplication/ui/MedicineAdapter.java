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
    private List<com.example.myapplication.model.IntakeLog> intakeLogs = new ArrayList<>();
    private final OnMedicineActionListener listener;

    public interface OnMedicineActionListener {
        void onReorderClick(Medicine medicine);
        void onTakenClick(Medicine medicine, boolean isWithinWindow);
        void onDeleteClick(Medicine medicine);
    }

    public MedicineAdapter(OnMedicineActionListener listener) {
        this.listener = listener;
    }

    public void setMedicines(List<Medicine> medicines, List<com.example.myapplication.model.IntakeLog> logs) {
        this.medicines = medicines;
        this.intakeLogs = logs != null ? logs : new ArrayList<>();
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

        updateIntakeUI(holder, medicine);

        holder.binding.btnMarkTaken.setOnClickListener(v -> {
            // Re-verify window on click for safety
            boolean canTakeNow = isWithinDosingWindow(medicine.getIntakeTimes());
            listener.onTakenClick(medicine, canTakeNow);
            if (canTakeNow) {
                holder.binding.btnMarkTaken.setEnabled(false);
                holder.binding.btnMarkTaken.setText("Taken");
            }
        });

        holder.binding.btnDeleteMed.setOnClickListener(v -> listener.onDeleteClick(medicine));
        holder.binding.ivMedTypeIcon.setOnClickListener(null);
    }

    private boolean isWithinDosingWindow(String intakeTimes) {
        if (intakeTimes == null || intakeTimes.isEmpty()) return false;
        String[] times = intakeTimes.split(",");
        java.util.Calendar now = java.util.Calendar.getInstance();
        int nowTotal = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE);
        String[] formats = {"hh:mm a", "h:mm a", "hh:mm aa", "h:mm aa", "HH:mm", "H:mm"};

        for (String t : times) {
            t = t.trim();
            for (String format : formats) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.US);
                    java.util.Date date = sdf.parse(t);
                    if (date != null) {
                        java.util.Calendar schedCal = java.util.Calendar.getInstance();
                        schedCal.setTime(date);
                        int schedTotal = schedCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + schedCal.get(java.util.Calendar.MINUTE);
                        if (Math.abs(schedTotal - nowTotal) <= 60) return true;
                    }
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    private boolean isAlreadyTaken(int medId, int schedTotalMinutes) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayYear = cal.get(java.util.Calendar.YEAR);
        int todayDay = cal.get(java.util.Calendar.DAY_OF_YEAR);

        for (com.example.myapplication.model.IntakeLog log : intakeLogs) {
            if (log.getMedicineId() == medId) {
                java.util.Calendar logCal = java.util.Calendar.getInstance();
                logCal.setTimeInMillis(log.getTimestamp());
                
                // Check if log is from today
                if (logCal.get(java.util.Calendar.YEAR) == todayYear && 
                    logCal.get(java.util.Calendar.DAY_OF_YEAR) == todayDay) {
                    
                    int logTotal = logCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + logCal.get(java.util.Calendar.MINUTE);
                    // If log is within 60 mins of this specific schedule, it's taken
                    if (Math.abs(logTotal - schedTotalMinutes) <= 60) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updateIntakeUI(MedicineViewHolder holder, Medicine medicine) {
        try {
            String intakeTimes = medicine.getIntakeTimes();
            if (intakeTimes == null || intakeTimes.isEmpty()) {
                holder.binding.tvNextIntakeTime.setText("No schedule set");
                holder.binding.btnMarkTaken.setVisibility(View.GONE);
                holder.binding.tvNextReminder.setVisibility(View.GONE);
                return;
            }

            String[] times = intakeTimes.split(",");
            java.util.Calendar now = java.util.Calendar.getInstance();
            int nowTotal = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE);

            String bestNextTime = null;
            int minFutureDiff = Integer.MAX_VALUE;
            String earliestTimeToday = null;
            int minTotalToday = Integer.MAX_VALUE;
            boolean canTakeNow = false;
            boolean alreadyTakenNow = false;

            String[] formats = {"hh:mm a", "h:mm a", "hh:mm aa", "h:mm aa", "HH:mm", "H:mm"};

            for (String t : times) {
                t = t.trim();
                if (t.isEmpty()) continue;
                
                java.util.Date date = null;
                for (String format : formats) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.US);
                        date = sdf.parse(t);
                        if (date != null) break;
                    } catch (Exception ignored) {}
                }

                if (date != null) {
                    java.util.Calendar schedCal = java.util.Calendar.getInstance();
                    schedCal.setTime(date);
                    int schedTotal = schedCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + schedCal.get(java.util.Calendar.MINUTE);

                    int diff = schedTotal - nowTotal;
                    
                    if (Math.abs(diff) <= 60) {
                        canTakeNow = true;
                        if (isAlreadyTaken(medicine.getId(), schedTotal)) {
                            alreadyTakenNow = true;
                        }
                    }

                    if (diff > 0 && diff < minFutureDiff) {
                        minFutureDiff = diff;
                        bestNextTime = t;
                    }
                    if (schedTotal < minTotalToday) {
                        minTotalToday = schedTotal;
                        earliestTimeToday = t;
                    }
                }
            }

            String resultText;
            if (bestNextTime != null) {
                resultText = "Today, " + bestNextTime;
            } else if (earliestTimeToday != null) {
                resultText = "Tomorrow, " + earliestTimeToday;
            } else {
                resultText = "Schedule Error";
            }

            holder.binding.tvNextIntakeTime.setText(resultText);
            holder.binding.tvNextReminder.setText("Scheduled: " + intakeTimes);
            holder.binding.tvNextReminder.setVisibility(View.VISIBLE);

            if (canTakeNow && !alreadyTakenNow) {
                holder.binding.btnMarkTaken.setVisibility(View.VISIBLE);
                holder.binding.btnMarkTaken.setEnabled(true);
                holder.binding.btnMarkTaken.setText("TAKEN");
            } else if (alreadyTakenNow) {
                holder.binding.btnMarkTaken.setVisibility(View.VISIBLE);
                holder.binding.btnMarkTaken.setEnabled(false);
                holder.binding.btnMarkTaken.setText("TAKEN");
            } else {
                holder.binding.btnMarkTaken.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.binding.tvNextIntakeTime.setText("Error updating");
            holder.binding.btnMarkTaken.setVisibility(View.GONE);
        }
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
