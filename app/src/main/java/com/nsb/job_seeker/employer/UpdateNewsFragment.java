package com.nsb.job_seeker.employer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.seeder.CustomRequest;
import com.nsb.job_seeker.seeder.JobDetailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class UpdateNewsFragment extends Fragment {
    private View updateNewsView;
    private EditText edtNameJob, edtPlace, edtSalary, edtWorkTime, edtJobField, edtDeadLine,
            edtDetailJob, edtJobReq, edtSkill;
    private ImageView icCancel, icCreateRec;
    private TextView tvTimeCreate;
    private ProgressBar loadingPB;
    private String url = "https://job-delta.vercel.app/job/create";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        updateNewsView = inflater.inflate(R.layout.fragment_employer_update_news, container, false);
        setControl();
        setEvent();
        return updateNewsView;
    }

    private void setControl() {
        edtNameJob = updateNewsView.findViewById(R.id.edt_name_job);
        edtPlace = updateNewsView.findViewById(R.id.edt_place);
        edtSalary = updateNewsView.findViewById(R.id.edt_salary);
        edtWorkTime = updateNewsView.findViewById(R.id.edt_work_time);
        edtJobField = updateNewsView.findViewById(R.id.edt_field_job);
        edtDetailJob = updateNewsView.findViewById(R.id.edt_detail_job);
        edtJobReq = updateNewsView.findViewById(R.id.edt_job_req);
        edtSkill = updateNewsView.findViewById(R.id.edt_skill);
        icCancel = updateNewsView.findViewById(R.id.ic_cancel);
        edtDeadLine = updateNewsView.findViewById(R.id.edt_deadline);
        tvTimeCreate = updateNewsView.findViewById(R.id.tv_time_create_new);
        icCreateRec = updateNewsView.findViewById(R.id.ic_create_recruitment);
        loadingPB = updateNewsView.findViewById(R.id.idLoadingPB);
    }

    private void setEvent() {

        //set porting date
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date(System.currentTimeMillis());
        tvTimeCreate.setText(formatter.format(date));


        icCreateRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                    String nameJob = edtNameJob.getText().toString();
                    String place = edtPlace.getText().toString();
                    String salary = edtSalary.getText().toString();
                    String timeWork = edtWorkTime.getText().toString();
                    String portingDate = tvTimeCreate.getText().toString();
                    String deadLine = edtDeadLine.getText().toString();
                    String descJob = formatString(edtDetailJob.getText().toString());
                    String jobReq = formatString(edtJobReq.getText().toString());

                    createRecruitment(nameJob, place, salary, timeWork, portingDate, deadLine, descJob, jobReq);
                }
                else Toast.makeText(getActivity(), "Bạn chưa điền đủ thông tin !", Toast.LENGTH_SHORT).show();
            }
        });

        edtDeadLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        if (Program.checkValidDeadline(day, month + 1, year)) {
                            edtDeadLine.setText((month + 1) + "-" + day + "-" + year);
                        } else {
                            Toast.makeText(getActivity(), "Hạn nộp không hợp lệ, vui lòng chọn lại !", Toast.LENGTH_SHORT).show();
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



    private void createRecruitment(String nameJob, String address, String salary, String timeWork,
                                   String portingDate, String deadline, String descJob, String jobReq) {
        String access_token = Program.token;
//        loadingPB.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Create Successful!!!");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", access_token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String jsonBody = "{" +
                        "\"name\":\"" + nameJob + "\"," +
                        "\"description\":\"" + descJob + "\"," +
                        "\"requirement\":\"" + jobReq + "\"," +
                        "\"hourWorking\":\"" + timeWork + "\"," +
                        "\"postingDate\":\"" + portingDate + "\"," +
                        "\"deadline\":\"" + deadline + "\"," +
                        "\"salary\":\"" + salary + "\"," +
                        "\"locationWorking\":\"" + address + "\"," +
                        "\"idOccupation\":\"64100616fdd527b2f096944a\"," +
                        "\"idcompany\":\"641684bda8922acf0dfc7a8c\"}";

                if ((getMethod() == Method.POST) && (jsonBody != null)) {
                    return jsonBody.getBytes();
                } else {
                    return super.getBody();
                }
            }
        };
        sr.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                System.out.println(error);
            }
        });
        queue.add(sr);
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

    private String formatString(String oldString) {
        String newString = "";
        String temp = "";
        String[] listString = oldString.substring(1).split("•");
        for (int i = 0; i < listString.length; i++) {
            temp = listString[i].trim().replace(".", "");
            newString = newString + temp + ".";
        }
        System.out.println(newString);
        return newString;
    }

    private void showIconCancel() {
        if (edtDetailJob.getText().toString() != "" || edtNameJob.getText().toString() != "" ||
                edtPlace.getText().toString() != "" || edtSalary.getText().toString() != "" ||
                edtWorkTime.getText().toString() != "" || edtJobField.getText().toString() != "" ||
                tvTimeCreate.getText().toString() != "" || edtDeadLine.getText().toString() != "" ||
                edtDetailJob.getText().toString() != "" || edtJobReq.getText().toString() != "" ||
                edtSkill.getText().toString() != "") {

        }
    }

    private boolean validate() {
        if (edtDetailJob.getText().toString() != "" || edtNameJob.getText().toString() != "" ||
                edtPlace.getText().toString() != "" || edtSalary.getText().toString() != "" ||
                edtWorkTime.getText().toString() != "" || edtDeadLine.getText().toString() != "" ||
                edtDetailJob.getText().toString() != "" || edtJobReq.getText().toString() != "") {
            return false;
        }
        return true;
    }


}
