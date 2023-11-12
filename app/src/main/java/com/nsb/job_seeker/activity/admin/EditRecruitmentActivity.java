package com.nsb.job_seeker.activity.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.nsb.job_seeker.activity.BaseActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentEmployerUpdateNewsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRecruitmentActivity extends BaseActivity {
    private FragmentEmployerUpdateNewsBinding binding;
    private String url = Constant.url_dev + "/job/update";
    private String idJob = "";
    private List<String> nameTypeJobs;
    private List<String> idTypeJobs;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = FragmentEmployerUpdateNewsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();
        setEvent();
    }

    private void setControl() {
        idTypeJobs = new ArrayList<>();
        nameTypeJobs = new ArrayList<>();
        nameTypeJobs.add("Lĩnh vực");

        preferenceManager = new PreferenceManager(this);
    }

    private void setEvent() {
//        icBack.setImageResource(R.drawable.ic_back);
//        icBack.setVisibility(View.VISIBLE);
//        tvTitle.setText("Chỉnh sửa tin tuyển dụng");
//
//        icBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        getTypeJob();

        setValue();

        pickTimeUpdate();

        formatBullet();

        clickUpdate();

    }

    private void clickUpdate() {
//        icUpdateRec.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String nameJob = edtNameJob.getText().toString();
//                String place = edtPlace.getText().toString();
//                String salary = edtSalary.getText().toString();
//                String timeWork = edtTimeWork.getText().toString();
//                String deadLine = edtDeadLine.getText().toString();
//                String descJob = Constant.formatStringFromBullet(edtDescJob.getText().toString());
//                String jobReq = Constant.formatStringFromBullet(edtJobReq.getText().toString());
//                String idTypeJob = idTypeJobs.get(spnTypeJob.getSelectedItemPosition() - 1);
//
//                try {
//                    updateRecruitmentApi(idJob, nameJob, place, salary, timeWork, deadLine, descJob, jobReq, idTypeJob);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private void updateRecruitmentApi(String id, String nameJob, String place, String salary, String timeWork,
                                      String deadline, String descJob, String jobReq, String idTypeJob) throws JSONException {
        String access_token = preferenceManager.getString(Constant.TOKEN);
        System.out.println(access_token);
//        loadingPB.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(EditRecruitmentActivity.this);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("_id", id);
        jsonObject.put("name", nameJob);
        jsonObject.put("locationWorking", place);
        jsonObject.put("salary", salary);
        jsonObject.put("hourWorking", timeWork);
        jsonObject.put("deadline", deadline);
        jsonObject.put("description", descJob);
        jsonObject.put("requirement", jobReq);
        jsonObject.put("idOccupation", idTypeJob);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.PUT, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                Toast.makeText(EditRecruitmentActivity.this, "Chỉnh sửa thành công!!!", Toast.LENGTH_SHORT).show();
//                Intent i = new Intent(EditRecruitmentActivity.this, RecruitmentDetailActivity.class);
//                i.putExtra("id", id);
//                i.putExtra("isChange", "true");
//                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof com.android.volley.NoConnectionError) {

                } else
                if(error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 401) {
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        preferenceManager.clear();
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                }
                System.out.println(error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", access_token);
                return headers;
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
                throw new VolleyError(error.getMessage());
            }
        });
        queue.add(sr);
    }

    private void setValue() {
    }

    private void pickTimeUpdate() {
//        edtDeadLine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Calendar calendar = Calendar.getInstance();
//                int currentYear = calendar.get(Calendar.YEAR);
//                int currentMonth = calendar.get(Calendar.MONTH);
//                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
//
//                DatePickerDialog datePickerDialog = new DatePickerDialog(EditRecruitmentActivity.this, new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//
//                        if (Constant.checkValidDeadline(day, month + 1, year)) {
//                            edtDeadLine.setText((month + 1) + "-" + day + "-" + year);
//                        } else {
//                            Toast.makeText(EditRecruitmentActivity.this, "Hạn nộp không hợp lệ, vui lòng chọn lại !", Toast.LENGTH_SHORT).show();
//                            edtDeadLine.requestFocus();
//                        }
//                    }
//                }, currentYear, currentMonth, currentDay);
//                datePickerDialog.show();
//
//            }
//        });
    }

    private void formatBullet() {
//        edtDescJob.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
//                Constant.addNewLine(text, lengthBefore, lengthAfter, edtDescJob);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        edtJobReq.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
//                Constant.addNewLine(text, lengthBefore, lengthAfter, edtJobReq);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    private boolean validate() {
//        if (isEmpty(edtDescJob) || isEmpty(edtNameJob) ||
//                isEmpty(edtPlace) || isEmpty(edtSalary) ||
//                isEmpty(edtDescJob) || isEmpty(edtDeadLine) ||
//                isEmpty(edtJobReq)) {
//            return false;
//        }
        return true;
    }

    private void getTypeJob() {

        String urlTypeJob = Constant.url_dev + "/occupation/list";
        RequestQueue queue = Volley.newRequestQueue(EditRecruitmentActivity.this);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, urlTypeJob, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray listTypeJob = response.getJSONObject("data").getJSONArray("data");
                    for (int i = 0; i < listTypeJob.length(); i++) {
                        JSONObject typeJob = listTypeJob.getJSONObject(i);
                        if (typeJob.getString("isDelete").equals("false")) {
                            nameTypeJobs.add(typeJob.getString("name"));
                            idTypeJobs.add(typeJob.getString("_id"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof com.android.volley.NoConnectionError) {

                }
                System.out.println(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
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
                throw new VolleyError(error.getMessage());
            }
        });
        queue.add(sr);
    }

}
