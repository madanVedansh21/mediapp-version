package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.databinding.ActivityMedicineListBinding;
import com.example.myapplication.model.Medicine;

public class MedicineListActivity extends AppCompatActivity implements MedicineAdapter.OnMedicineActionListener {
    private ActivityMedicineListBinding binding;
    private MediBuddyViewModel viewModel;
    private MedicineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);
        adapter = new MedicineAdapter(this);

        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMedicines.setAdapter(adapter);

        viewModel.getAllMedicines().observe(this, medicines -> {
            adapter.setMedicines(medicines);
        });
    }

    @Override
    public void onReorderClick(Medicine medicine) {
        Toast.makeText(this, "Reordering " + medicine.getName() + "...", Toast.LENGTH_SHORT).show();
        viewModel.reorderMedicine(medicine);
    }

    @Override
    public void onTakenClick(Medicine medicine) {
        if (medicine.getStock() > 0) {
            medicine.setStock(medicine.getStock() - 1);
            viewModel.updateMedicine(medicine);
            viewModel.logIntake(new com.example.myapplication.model.IntakeLog(medicine.getId(), medicine.getName(), System.currentTimeMillis(), "Taken"));
            Toast.makeText(this, "Dose marked as taken", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Out of stock! Please reorder.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(Medicine medicine) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Are you sure you want to delete " + medicine.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteMedicine(medicine);
                    Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
