package com.nsb.job_seeker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.ActivityNameCompanyBinding;
import com.nsb.job_seeker.model.Company;

import java.io.Serializable;

public class NameCompanyActivity extends AppCompatActivity {
    private ActivityNameCompanyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityNameCompanyBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        clickNext();
    }

    private void clickNext() {
        binding.buttonNext.setOnClickListener(v -> {
            if (binding.textNameCompany.getText().toString().isEmpty()) {
                binding.layoutErrorName.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorName.setVisibility(View.GONE);
                Company company = new Company();
                company.setName(binding.textNameCompany.getText().toString().trim());
                Intent intent = new Intent(NameCompanyActivity.this, AddressCompanyActivity.class);
                intent.putExtra(Constant.COMPANY_MODEL, (Serializable) company);
                startActivity(intent);
            }
        });
    }
}