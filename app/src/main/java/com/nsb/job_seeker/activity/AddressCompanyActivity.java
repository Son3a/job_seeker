package com.nsb.job_seeker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.ActivityAddressCompanyBinding;
import com.nsb.job_seeker.model.Company;

import java.io.Serializable;

public class AddressCompanyActivity extends AppCompatActivity {
    private ActivityAddressCompanyBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAddressCompanyBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        clickNext();
    }

    private void clickNext() {
        binding.buttonNext.setOnClickListener(v -> {
            if (binding.textAddressCompany.getText().toString().isEmpty()) {
                binding.layoutErrorAddress.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorAddress.setVisibility(View.GONE);
                Company company = (Company) getIntent().getSerializableExtra(Constant.COMPANY_MODEL);
                company.setAddress(binding.textAddressCompany.getText().toString().trim());
                Intent intent = new Intent(AddressCompanyActivity.this, NumEmployerActivity.class);
                intent.putExtra(Constant.COMPANY_MODEL, (Serializable) company);
                startActivity(intent);
            }
        });
    }
}