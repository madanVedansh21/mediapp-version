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
                List<LogDisplayItem> displayLogs = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                for (IntakeLog log : logs) {
                    displayLogs.add(new LogDisplayItem(
                        log.getMedicineName(),
                        sdf.format(new Date(log.getTimestamp())),
                        log.getStatus()
                    ));
                }
                adapter.setData(displayLogs);
            });
        } else {
            viewModel.getAllSymptomLogs().observe(this, logs -> {
                List<LogDisplayItem> displayLogs = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                for (SymptomLog log : logs) {
                    displayLogs.add(new LogDisplayItem(
                        log.getCategory(),
                        log.getNotes() + " (" + sdf.format(new Date(log.getTimestamp())) + ")",
                        "Level " + log.getSeverity()
                    ));
                }
                adapter.setData(displayLogs);
            });
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
