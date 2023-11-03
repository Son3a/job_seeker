package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.os.Bundle;

import com.nsb.job_seeker.activity.BaseActivity;

import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityPreviousLoginBinding;

public class PreviousLoginActivity extends BaseActivity {
    ActivityPreviousLoginBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityPreviousLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        clickLogin();
        clickRegister();

    }

    private void clickLogin(){
        binding.btnLogin.setOnClickListener(v->{
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void clickRegister(){
        binding.btnRegister.setOnClickListener(v->{
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

}