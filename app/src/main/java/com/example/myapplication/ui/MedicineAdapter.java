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

        // --- NEW CONDITIONAL RENDERING FOR "MARK AS TAKEN" ---
        // This simulates checking if it's within a 1-hour window of the scheduled intakeTimes
        boolean isTimeForDose = checkIfTimeForDose(medicine.getIntakeTimes());
        
        if (isTimeForDose) {
            holder.binding.btnMarkTaken.setVisibility(View.VISIBLE);
            holder.binding.tvNextReminder.setVisibility(View.GONE);
        } else {
            holder.binding.btnMarkTaken.setVisibility(View.GONE);
            holder.binding.tvNextReminder.setVisibility(View.VISIBLE);
            holder.binding.tvNextReminder.setText("Next dose scheduled: " + medicine.getIntakeTimes());
        }

        holder.binding.btnMarkTaken.setOnClickListener(v -> listener.onTakenClick(medicine));
        
        // Remove click listener from the icon to fix the "accidental reduction" bug
        holder.binding.ivMedTypeIcon.setOnClickListener(null);
    }

    private boolean checkIfTimeForDose(String intakeTimes) {
        if (intakeTimes == null || intakeTimes.isEmpty()) return false;
        
        // Simple mock logic: If current minute is even, show button (for demo purposes)
        // In a real app, you'd parse "08:00, 20:00" and compare with java.util.Calendar
        return java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE) % 2 == 0;
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
