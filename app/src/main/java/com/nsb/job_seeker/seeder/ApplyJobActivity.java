package com.nsb.job_seeker.seeder;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.R;

public class ApplyJobActivity extends AppCompatActivity {
    private ImageView imgBack;
    private Button btnSendCv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_apply_job);

        setControl();
        setEvent();
    }

    private void setControl() {
        imgBack = findViewById(R.id.ic_back);
        btnSendCv = findViewById(R.id.btn_send_cv);
    }

    private void setEvent() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
