package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.activity.BaseActivity;

import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityPreviousLoginBinding;

public class PreviousLoginActivity extends AppCompatActivity {
    ActivityPreviousLoginBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityPreviousLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);

        clickLogin();
        clickRegister();
        gotoAppWithoutLogin();
    }

    private void gotoAppWithoutLogin() {
        binding.textVisitPage.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeekerMainActivity.class);
            preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void clickLogin() {
        binding.btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void clickRegister() {
        binding.btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

}