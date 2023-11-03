package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nsb.job_seeker.activity.BaseActivity;

import com.nsb.job_seeker.R;

public class ConfirmOTP extends BaseActivity {
    private Button btnConfirmOTP;
    private EditText edtOTP;
    private TextView txtWarning;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

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