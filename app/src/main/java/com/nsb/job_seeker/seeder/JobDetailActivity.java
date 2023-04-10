package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.R;

public class JobDetailActivity extends AppCompatActivity {
    private ImageView imgBack;
    private Button btnApply;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_job_detail);

        setControl();
        setEvent();
    }

    private void setControl() {
        imgBack = (ImageView) findViewById(R.id.ic_back);
        btnApply = findViewById(R.id.btn_apply);
    }



    private void setEvent() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
                startActivity(i);
            }
        });
    }
}
