package com.nsb.job_seeker.employer;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;

import java.util.Calendar;

public class EditRecruitmentActivity extends AppCompatActivity {
    private EditText edtNameJob, edtPlace, edtSalary, edtWorkTime, edtJobField, edtDeadLine,
            edtDetailJob, edtJobReq, edtSkill;
    private ImageView icBack, icUpdateRec;
    private TextView tvTimeCreate, tvTitle;
    private ProgressBar loadingPB;
    private String url = "https://job-delta.vercel.app/job/create";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.fragment_employer_update_news);

        setControl();
        setEvent();
    }

    private void setControl() {
        edtNameJob = findViewById(R.id.edt_name_job);
        edtPlace = findViewById(R.id.edt_place);
        edtSalary = findViewById(R.id.edt_salary);
        edtWorkTime = findViewById(R.id.edt_work_time);
        edtJobField = findViewById(R.id.edt_field_job);
        edtDetailJob = findViewById(R.id.edt_detail_job);
        edtJobReq = findViewById(R.id.edt_job_req);
        edtSkill = findViewById(R.id.edt_skill);
        icBack = findViewById(R.id.ic_cancel);
        edtDeadLine = findViewById(R.id.edt_deadline);
        tvTimeCreate = findViewById(R.id.tv_time_create_new);
        icUpdateRec = findViewById(R.id.ic_create_recruitment);
        loadingPB = findViewById(R.id.idLoadingPB);
        tvTitle = findViewById(R.id.txt_title);
    }

    private void setEvent() {
        icBack.setImageResource(R.drawable.ic_back);
        tvTitle.setText("Chỉnh sửa tin tuyển dụng");
        icBack.setVisibility(View.VISIBLE);
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edtDeadLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditRecruitmentActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        if (Program.checkValidDeadline(day, month + 1, year)) {
                            edtDeadLine.setText((month + 1) + "-" + day + "-" + year);
                        } else {
                            Toast.makeText(EditRecruitmentActivity.this, "Hạn nộp không hợp lệ, vui lòng chọn lại !", Toast.LENGTH_SHORT).show();
                            edtDeadLine.requestFocus();
                        }
                    }
                }, currentYear, currentMonth, currentDay);
                datePickerDialog.show();

            }
        });

        edtDetailJob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                addNewLine(text, lengthBefore, lengthAfter, edtDetailJob);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtJobReq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                addNewLine(text, lengthBefore, lengthAfter, edtJobReq);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtSkill.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                addNewLine(text, lengthBefore, lengthAfter, edtSkill);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void addNewLine(CharSequence text, int lengthBefore, int lengthAfter, EditText edtText) {
        if (lengthAfter > lengthBefore) {
            if (text.toString().length() == 1) {
                text = "• " + text;
                edtText.setText(text);
                edtText.setSelection(edtText.getText().length());
            }

            if (text.toString().endsWith("\n")) {
                text = text.toString().replace("\n", "\n• ");
                text = text.toString().replace("• •", "•");
                text = text.toString().replace("\n• \n• ", "\n• ");
                text = text.toString().replace("• \n• ", "• ");
                edtText.setText(text);
                edtText.setSelection(edtText.getText().length());
            }
        }
    } 
}
