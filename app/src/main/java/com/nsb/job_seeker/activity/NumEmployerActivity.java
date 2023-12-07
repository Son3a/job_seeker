package com.nsb.job_seeker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.ActivityNumEmployerBinding;
import com.nsb.job_seeker.model.Company;

import java.io.Serializable;

public class NumEmployerActivity extends AppCompatActivity {
    private ActivityNumEmployerBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityNumEmployerBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        clickNext();
    }

    private void clickNext() {
        binding.buttonNext.setOnClickListener(v -> {
            if (binding.textNumEmployer.getText().toString().isEmpty()) {
                binding.layoutErrorNum.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorNum.setVisibility(View.GONE);
                Company company = (Company) getIntent().getSerializableExtra(Constant.COMPANY_MODEL);
                company.setTotalEmployee(Integer.parseInt(binding.textNumEmployer.getText().toString().trim()));
                Intent intent = new Intent(NumEmployerActivity.this, IntroduceCompanyActivity.class);
                intent.putExtra(Constant.COMPANY_MODEL, (Serializable) company);
                startActivity(intent);
            }
        });
    }
}