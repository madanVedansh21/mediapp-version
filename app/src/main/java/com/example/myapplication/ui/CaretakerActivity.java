package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.ActivityCaretakerBinding;
import com.example.myapplication.model.User;
import com.example.myapplication.util.ValidationUtils;

public class CaretakerActivity extends AppCompatActivity {
    private ActivityCaretakerBinding binding;
    private MediBuddyViewModel viewModel;
    private User currentUser;
    private static final String[] COUNTRY_CODES = {
            "+1", "+44", "+61", "+65", "+81", "+91", "+971"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaretakerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ArrayAdapter<String> countryCodeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                COUNTRY_CODES
        );
        binding.actvCountryCode.setAdapter(countryCodeAdapter);
        binding.actvCountryCode.setText("+91", false);

        viewModel = new ViewModelProvider(this).get(MediBuddyViewModel.class);

        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                String preferredPhone = user.getHospitalPhone();
                if (preferredPhone == null || preferredPhone.isEmpty()) {
                    preferredPhone = user.getCaretakerPhone();
                }
                if (preferredPhone == null || preferredPhone.isEmpty()) {
                    preferredPhone = user.getDoctorPhone();
                }
                String[] split = ValidationUtils.splitCountryCodeAndNumber(preferredPhone);
                binding.actvCountryCode.setText(split[0], false);

                binding.etCaretakerName.setText(user.getCaretakerName());
                binding.etCaretakerPhone.setText(ValidationUtils.splitCountryCodeAndNumber(user.getCaretakerPhone())[1]);
                binding.etCaretakerEmail.setText(user.getCaretakerEmail());
                binding.etHospitalName.setText(user.getHospital());
                binding.etHospitalPhone.setText(ValidationUtils.splitCountryCodeAndNumber(user.getHospitalPhone())[1]);
                binding.etDoctorName.setText(user.getDoctorName());
                binding.etDoctorPhone.setText(ValidationUtils.splitCountryCodeAndNumber(user.getDoctorPhone())[1]);
            }
        });

        binding.btnSaveCaretaker.setOnClickListener(v -> {
            if (currentUser != null) {
                String countryCode = ValidationUtils.sanitize(binding.actvCountryCode.getText().toString());
                if (countryCode.isEmpty()) {
                    countryCode = "+91";
                }

                String caretakerPhone = ValidationUtils.sanitize(binding.etCaretakerPhone.getText().toString());
                String hospitalPhone = ValidationUtils.sanitize(binding.etHospitalPhone.getText().toString());
                String doctorPhone = ValidationUtils.sanitize(binding.etDoctorPhone.getText().toString());

                String normalizedCaretakerPhone = caretakerPhone.isEmpty() ? "" : ValidationUtils.normalizePhoneWithCountryCode(countryCode, caretakerPhone);
                String normalizedHospitalPhone = hospitalPhone.isEmpty() ? "" : ValidationUtils.normalizePhoneWithCountryCode(countryCode, hospitalPhone);
                String normalizedDoctorPhone = doctorPhone.isEmpty() ? "" : ValidationUtils.normalizePhoneWithCountryCode(countryCode, doctorPhone);

                if (!caretakerPhone.isEmpty() && normalizedCaretakerPhone == null) {
                    binding.etCaretakerPhone.setError("Use at least 10 digits");
                    binding.etCaretakerPhone.requestFocus();
                    return;
                }

                if (!hospitalPhone.isEmpty() && normalizedHospitalPhone == null) {
                    binding.etHospitalPhone.setError("Hospital phone must be at least 10 digits");
                    binding.etHospitalPhone.requestFocus();
                    return;
                }

                if (!doctorPhone.isEmpty() && normalizedDoctorPhone == null) {
                    binding.etDoctorPhone.setError("Use at least 10 digits");
                    binding.etDoctorPhone.requestFocus();
                    return;
                }

                User updatedUser = new User(
                        currentUser.getName(),
                        currentUser.getEmail(),
                        currentUser.getPassword(),
                        currentUser.getEmergencyContact(),
                        binding.etCaretakerName.getText().toString(),
                        normalizedCaretakerPhone,
                        binding.etCaretakerEmail.getText().toString(),
                        binding.etHospitalName.getText().toString(),
                        normalizedHospitalPhone,
                        binding.etDoctorName.getText().toString(),
                        normalizedDoctorPhone,
                        currentUser.getDoctorEmail() // Preserving old or add field if needed
                );
                updatedUser.setId(currentUser.getId());
                viewModel.updateUser(updatedUser);
                Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
