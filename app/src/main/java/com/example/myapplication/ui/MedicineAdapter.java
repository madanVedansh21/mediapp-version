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
        holder.binding.tvMedDetails.setText("Dosage: " + medicine.getDosage() + " | Type: " + medicine.getType());
        holder.binding.tvStock.setText("Stock: " + medicine.getStock());

        holder.binding.btnTaken.setOnClickListener(v -> listener.onTakenClick(medicine));

        if (medicine.getStock() <= medicine.getThreshold()) {
            holder.binding.btnReorder.setVisibility(View.VISIBLE);
            holder.binding.btnReorder.setOnClickListener(v -> listener.onReorderClick(medicine));
        } else {
            holder.binding.btnReorder.setVisibility(View.GONE);
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
