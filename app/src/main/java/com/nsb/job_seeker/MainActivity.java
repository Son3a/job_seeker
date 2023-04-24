package com.nsb.job_seeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.employer.EmployerMainActivity;
import com.nsb.job_seeker.seeder.SeekerMainActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnSeeker, btnEmployer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setControl();
        setEvent();
    }

    private void setControl(){
        btnEmployer = findViewById(R.id.btn_employer);
        btnSeeker = findViewById(R.id.btn_seeker);
    }

    private void setEvent(){
        btnSeeker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SeekerMainActivity.class);
                startActivity(i);
            }
        });

        btnEmployer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, EmployerMainActivity.class);
                startActivity(i);
            }
        });
    }
}
