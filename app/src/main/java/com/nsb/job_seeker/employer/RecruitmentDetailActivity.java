package com.nsb.job_seeker.employer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.R;

public class RecruitmentDetailActivity extends AppCompatActivity {
    private ImageView icBack, icListCV, icDelete, icEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_recruitment_detail);

        setControl();
        setEvent();
    }

    private void setControl() {
        icBack = findViewById(R.id.ic_back_rec_detail);
        icListCV = findViewById(R.id.ic_list_cv);
        icDelete = findViewById(R.id.ic_delete_recruitment);
        icEdit = findViewById(R.id.ic_edit_recruitment);
    }

    private void setEvent() {
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        icListCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecruitmentDetailActivity.this, ListCVActivity.class);
                startActivity(i);
            }
        });

        icDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDelete("Bạn có muốn xóa tin tuyển dụng này không ?");
            }
        });

        icEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecruitmentDetailActivity.this, EditRecruitmentActivity.class);
                startActivity(i);
            }
        });
    }

    private void showDialogDelete(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(RecruitmentDetailActivity.this)
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void deleteRecuitment() {
    }
}
