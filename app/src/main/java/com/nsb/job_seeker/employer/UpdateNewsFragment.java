package com.nsb.job_seeker.employer;

import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.R;

import java.io.PrintStream;

public class UpdateNewsFragment extends Fragment {
    private View updateNewsView;
    private EditText edtNameJob, edtPlace, edtSalary, edtWorkTime, edtJobField, edtTimeCreate, edtDeadLine,
            edtDetailJob, edtJobReq, edtSkill;
    private ImageView icCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        updateNewsView = inflater.inflate(R.layout.fragment_employer_update_news, container, false);
        setControl();
        setEvent();
        return updateNewsView;
    }

    private void setControl() {
        edtDetailJob = updateNewsView.findViewById(R.id.edt_detail_job);
        edtJobReq = updateNewsView.findViewById(R.id.edt_job_req);
        edtSkill = updateNewsView.findViewById(R.id.edt_skill);
        icCancel = updateNewsView.findViewById(R.id.ic_cancel);
    }

    private void setEvent() {
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

            if (edtText.toString() != "") {
                icCancel.setVisibility(View.VISIBLE);
            } else {
                icCancel.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showIconCancel() {
        if (edtDetailJob.getText().toString() != "" || edtNameJob.getText().toString() != "" ||
                edtPlace.getText().toString() != "" || edtSalary.getText().toString() != "" ||
                edtWorkTime.getText().toString() != "" || edtJobField.getText().toString() != "" ||
                edtTimeCreate.getText().toString() != "" || edtDeadLine.getText().toString() != "" ||
                edtDetailJob.getText().toString() != "" || edtJobReq.getText().toString() != "" ||
                edtSkill.getText().toString() != "") {

        }
    }


}
