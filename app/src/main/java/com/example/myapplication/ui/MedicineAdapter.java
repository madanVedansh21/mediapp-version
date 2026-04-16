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
            
            // Reorder button logic when low stock
            holder.binding.llRefillAlert.setOnClickListener(v -> listener.onReorderClick(medicine));
        } else {
            holder.binding.llRefillAlert.setVisibility(View.GONE);
            holder.binding.llStockContainer.setVisibility(View.VISIBLE);
        }

        // Set Icon based on type
        if ("capsule".equalsIgnoreCase(medicine.getType())) {
            holder.binding.ivMedTypeIcon.setImageResource(android.R.drawable.ic_menu_today); // Replace with capsule icon if available
        } else {
            holder.binding.ivMedTypeIcon.setImageResource(android.R.drawable.ic_menu_today);
        }

        // Only decrease stock when specifically clicking the "taken" action area or a specific button
        // For now, let's use the icon as the "mark as taken" trigger to avoid accidental clicks on the card
        holder.binding.ivMedTypeIcon.setOnClickListener(v -> listener.onTakenClick(medicine));
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
