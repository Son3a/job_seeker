package com.nsb.job_seeker.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.R;

public class ConfirmOTP extends AppCompatActivity {
    private Button btnConfirmOTP;
    private EditText edtOTP;
    private TextView txtWarning;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Mã xác thực OTP");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DB3AA1F6")));
        setContentView(R.layout.activity_confirm_otp);
        setControl();
        setEvent();
    }

    private void setControl() {
        btnConfirmOTP = findViewById(R.id.btnConfirmOTP);
        edtOTP = findViewById(R.id.edtOTP);
        txtWarning = findViewById(R.id.txtWarningCodeOTP);
    }

    private void setEvent() {
        btnConfirmOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtOTP.getText().toString().length() < 6) {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Bạn chưa nhập đủ 6 chữ số !");
                } else {
                    txtWarning.setVisibility(View.GONE);
                    Intent i = new Intent(ConfirmOTP.this, SetNewPassword.class);
                    i.putExtra("code", edtOTP.getText().toString());
                    startActivity(i);
                }
            }
        });
    }
}