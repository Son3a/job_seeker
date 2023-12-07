package com.nsb.job_seeker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.ActivityIntroduceCompanyBinding;
import com.nsb.job_seeker.model.Company;

import java.io.Serializable;

public class IntroduceCompanyActivity extends AppCompatActivity {
    private ActivityIntroduceCompanyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityIntroduceCompanyBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        clickNext();
    }

    private void clickNext() {
        binding.buttonNext.setOnClickListener(v -> {
            if (binding.textIntroduceCompany.getText().toString().isEmpty()) {
                binding.layoutErrorIntroduce.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorIntroduce.setVisibility(View.GONE);
                Company company = (Company) getIntent().getSerializableExtra(Constant.COMPANY_MODEL);
                company.setAbout(binding.textIntroduceCompany.getText().toString().trim());
                Intent intent = new Intent(IntroduceCompanyActivity.this, WebsiteCompanyActivity.class);
                intent.putExtra(Constant.COMPANY_MODEL, (Serializable) company);
                startActivity(intent);
            }
        });
    }
}