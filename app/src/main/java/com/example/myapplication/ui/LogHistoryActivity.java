package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityLogHistoryBinding;
import com.example.myapplication.model.IntakeLog;
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

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateList(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Default view
        updateList(0);
    }

    private void updateList(int position) {
        if (position == 0) {
            viewModel.getAllIntakeLogs().observe(this, logs -> {
                List<String> displayLogs = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                for (IntakeLog log : logs) {
                    displayLogs.add(log.getMedicineName() + " - " + log.getStatus() + "\n" + sdf.format(new Date(log.getTimestamp())));
                }
                adapter.setData(displayLogs);
            });
        } else {
            viewModel.getAllSymptomLogs().observe(this, logs -> {
                List<String> displayLogs = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                for (SymptomLog log : logs) {
                    displayLogs.add(log.getCategory() + " (Severity: " + log.getSeverity() + ")\n" + log.getNotes() + "\n" + sdf.format(new Date(log.getTimestamp())));
                }
                adapter.setData(displayLogs);
            });
        }
    }

    static class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
        private List<String> data = new ArrayList<>();

        public void setData(List<String> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(data.get(position));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) { super(itemView); }
        }
    }
}
