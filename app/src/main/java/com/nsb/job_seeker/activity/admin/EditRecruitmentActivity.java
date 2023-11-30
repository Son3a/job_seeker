package com.nsb.job_seeker.activity.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.adapter.FilterAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomDialogDelete;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentEmployerUpdateNewsBinding;
import com.nsb.job_seeker.listener.FilterListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRecruitmentActivity extends BaseActivity {
    private FragmentEmployerUpdateNewsBinding binding;
    private List<String> nameTypeJobs;
    private List<String> idTypeJobs;
    private PreferenceManager preferenceManager;
    private BottomSheetDialog bottomSheetUnitMoney, bottomSheetTypeJob, bottomSheetGender;
    private String idOccupation;
    private LoadingDialog loadingDialog;
    private FilterAdapter genderAdapter, typeJobAdapter, unitMoney;
    private List<Boolean> listSelectedGender, listSelectedTypeJob, listSelectedUnitMoney;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = FragmentEmployerUpdateNewsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();
        setEvent();
    }

    private void setControl() {
        loadingDialog = new LoadingDialog(EditRecruitmentActivity.this);
        preferenceManager = new PreferenceManager(EditRecruitmentActivity.this);
        initBottomUnitMoney();
        initBottomSheetGender();
    }


    private void setEvent() {
        //set porting date
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date(System.currentTimeMillis());

        loadContent();
        pickTime();
        formatBullet();
        openBottomSheetTypeJob();
        openBottomSheetGender();
        openBottomSheetMoney();
        clickUpdateJob();
        cancelProcess();
        getTypeJob();
    }

    private void loadContent() {
        try {
            Job job = (Job) getIntent().getSerializableExtra(Constant.JOB_MODEL);
            binding.edtNameJob.setText(job.getNameJob());
            binding.edtDetailJob.setText(Constant.formatStringToBullet(job.getDesJob()));
            binding.edtJobReq.setText(Constant.formatStringToBullet(job.getReqJob()));
            binding.edtDeadline.setText(Constant.formatTimeDDMMYYYY(job.getDeadline()));
            binding.textSalary.setText(Constant.formatSalary(job.getSalary()));
            binding.textPosition.setText(job.getPlace());
            binding.textGender.setText(job.getGender());
            binding.textExperience.setText(job.getExperience());
            binding.textWorkingForm.setText(job.getWorkingForm());
            binding.textApplyAmount.setText(job.getAmountRecruitment());
            binding.textTypeJob.setText(job.getTypeJob());
            idOccupation = job.getTypeId();

            if (job.getSalary().contains("VND")) {
                binding.textUnitMoney.setText("VND");
            } else if (job.getSalary().contains("USD")) {
                binding.textUnitMoney.setText("USD");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void cancelProcess() {
        binding.btnCancel.setOnClickListener(v -> {
            if (checkAllEmpty()) {
                return;
            }
            CustomDialogDelete customDialogDelete = new CustomDialogDelete(EditRecruitmentActivity.this, "Bạn chắc chắn muốn hủy quá trình này?",
                    "Có", "Không") {
                @Override
                public void doAccept() {
                    clearText();
                }

                @Override
                public void doCancel() {

                }
            };
            customDialogDelete.openDiaLogDelete();
        });
    }

    private void clearText() {
        binding.edtNameJob.setText("");
        binding.edtDetailJob.setText("");
        binding.edtJobReq.setText("");
        binding.edtDeadline.setText("");
        binding.textSalary.setText("");
        binding.textPosition.setText("");
        binding.textGender.setText("");
        binding.textExperience.setText("");
        binding.textWorkingForm.setText("");
        binding.textApplyAmount.setText("");
        binding.edtNameJob.clearFocus();
        binding.edtDetailJob.clearFocus();
        binding.edtJobReq.clearFocus();
        binding.edtDeadline.clearFocus();
        binding.textSalary.clearFocus();
        binding.textPosition.clearFocus();
        binding.textGender.clearFocus();
        binding.textExperience.clearFocus();
        binding.textWorkingForm.clearFocus();
        binding.textApplyAmount.clearFocus();
    }

    private void openBottomSheetTypeJob() {
        binding.textTypeJob.setOnClickListener(v -> {
            if (bottomSheetTypeJob != null) {
                bottomSheetTypeJob.show();
            }
        });
    }

    private void initBottomSheetGender() {
        bottomSheetGender = new BottomSheetDialog(EditRecruitmentActivity.this);
        View layoutGender = getLayoutInflater().inflate(R.layout.layout_filter, null);
        TextView textTitle = (TextView) layoutGender.findViewById(R.id.textTitle);
        textTitle.setText("Chọn giới tính");
        List<String> listGender = new ArrayList<>();
        listSelectedGender = new ArrayList<>();
        listGender.add("Không yêu cầu");
        listGender.add("Nam");
        listGender.add("Nữ");
        listSelectedGender.add(false);
        listSelectedGender.add(false);
        listSelectedGender.add(false);
        RecyclerView recyclerView = layoutGender.findViewById(R.id.rcvFilter);
        genderAdapter = new FilterAdapter(listGender, listSelectedGender, new FilterListener() {
            @Override
            public void onClickItem(String data, int position) {
                binding.textGender.setText(data);
                bottomSheetGender.dismiss();
            }
        });
        recyclerView.setAdapter(genderAdapter);
        bottomSheetGender.setContentView(layoutGender);

        ImageView imgClose = layoutGender.findViewById(R.id.imageClose);
        imgClose.setOnClickListener(view -> {
            bottomSheetGender.dismiss();
        });
    }

    private void openBottomSheetGender() {
        binding.textGender.setOnClickListener(v -> {
            if (bottomSheetGender != null) {
                bottomSheetGender.show();
            }
        });
    }

    private void initBottomSheetTypeJob() {
        bottomSheetTypeJob = new BottomSheetDialog(EditRecruitmentActivity.this);
        View layoutTypeJob = getLayoutInflater().inflate(R.layout.layout_filter, null);
        TextView textTitle = (TextView) layoutTypeJob.findViewById(R.id.textTitle);
        textTitle.setText("Chọn loại công việc");

        RecyclerView recyclerView = layoutTypeJob.findViewById(R.id.rcvFilter);
        typeJobAdapter = new FilterAdapter(nameTypeJobs, listSelectedTypeJob, new FilterListener() {
            @Override
            public void onClickItem(String data, int position) {
                binding.textTypeJob.setText(data);
                idOccupation = idTypeJobs.get(position);
                bottomSheetTypeJob.dismiss();
            }
        });
        ImageView imgClose = layoutTypeJob.findViewById(R.id.imageClose);
        imgClose.setOnClickListener(view -> {
            bottomSheetTypeJob.dismiss();
        });
        recyclerView.setAdapter(typeJobAdapter);
        typeJobAdapter.notifyDataSetChanged();
        bottomSheetTypeJob.setContentView(layoutTypeJob);
    }

    private void initBottomUnitMoney() {
        bottomSheetUnitMoney = new BottomSheetDialog(EditRecruitmentActivity.this);
        View layoutUnitMoney = getLayoutInflater().inflate(R.layout.layout_filter, null);
        TextView textTitle = (TextView) layoutUnitMoney.findViewById(R.id.textTitle);
        textTitle.setText("Chọn đơn vị tiền");
        List<String> listUnitMoney = new ArrayList<>();
        listSelectedUnitMoney = new ArrayList<>();
        listUnitMoney.add("VND");
        listUnitMoney.add("USD");
        listSelectedUnitMoney.add(false);
        listSelectedUnitMoney.add(false);
        RecyclerView recyclerView = layoutUnitMoney.findViewById(R.id.rcvFilter);
        unitMoney = new FilterAdapter(listUnitMoney, listSelectedUnitMoney, new FilterListener() {
            @Override
            public void onClickItem(String data, int position) {
                binding.textUnitMoney.setText(data);
                bottomSheetUnitMoney.dismiss();
            }
        });
        recyclerView.setAdapter(unitMoney);
        bottomSheetUnitMoney.setContentView(layoutUnitMoney);

        ImageView imgClose = layoutUnitMoney.findViewById(R.id.imageClose);
        imgClose.setOnClickListener(view -> {
            bottomSheetUnitMoney.dismiss();
        });
    }

    private void openBottomSheetMoney() {
        binding.textUnitMoney.setOnClickListener(v -> {
            if (bottomSheetUnitMoney != null) {
                bottomSheetUnitMoney.show();
            }
        });
    }

    private void pickTime() {
        binding.edtDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditRecruitmentActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        if (Constant.checkValidDeadline(day, month + 1, year)) {
                            binding.edtDeadline.setText(day + "-" + (month + 1) + "-" + year);
                        } else {
                            Toast.makeText(EditRecruitmentActivity.this, "Hạn nộp không hợp lệ, vui lòng chọn lại !", Toast.LENGTH_SHORT).show();
                            binding.edtDeadline.requestFocus();
                        }
                    }
                }, currentYear, currentMonth, currentDay);
                datePickerDialog.show();

            }
        });
    }

    private void formatBullet() {
        binding.edtDetailJob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                Constant.addNewLine(text, lengthBefore, lengthAfter, binding.edtDetailJob);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.edtJobReq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                Constant.addNewLine(text, lengthBefore, lengthAfter, binding.edtJobReq);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void clickUpdateJob() {
        binding.btnSave.setOnClickListener(v -> {
            validate();
            try {
                if (!checkExistEmpty()) {
                    Job job = (Job) getIntent().getSerializableExtra(Constant.JOB_MODEL);
                    updateRecruitment(
                            job.getId(),
                            binding.edtNameJob.getText().toString().trim(),
                            Constant.formatStringFromBullet(binding.edtDetailJob.getText().toString().trim()),
                            Constant.formatStringFromBullet(binding.edtJobReq.getText().toString().trim()),
                            Constant.formatTimeMMDDYYYY(binding.edtDeadline.getText().toString().trim()),
                            binding.textSalary.getText().toString().trim(),
                            binding.textPosition.getText().toString().trim(),
                            idOccupation,
                            preferenceManager.getString(Constant.COMPANY_ID),
                            binding.textGender.getText().toString().trim(),
                            binding.textExperience.getText().toString().trim(),
                            binding.textWorkingForm.getText().toString().trim(),
                            binding.textApplyAmount.getText().toString().trim());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean checkExistEmpty() {
        if (isEmpty(binding.edtNameJob) || isEmpty(binding.textPosition) || isEmpty(binding.textSalary) ||
                isEmpty(binding.textWorkingForm) || isEmpty(binding.textExperience) || isEmpty(binding.textTypeJob) ||
                isEmpty(binding.textApplyAmount) || isEmpty(binding.edtDeadline) || isEmpty(binding.edtDetailJob) ||
                isEmpty(binding.edtJobReq) || isEmpty(binding.textGender)) {
            return true;
        }
        return false;
    }

    private boolean checkAllEmpty() {
        if (!isEmpty(binding.edtNameJob) || !isEmpty(binding.textPosition) || !isEmpty(binding.textSalary) ||
                !isEmpty(binding.textWorkingForm) || !isEmpty(binding.textExperience) || !isEmpty(binding.textTypeJob) ||
                !isEmpty(binding.textApplyAmount) || !isEmpty(binding.edtDeadline) || !isEmpty(binding.edtDetailJob) ||
                !isEmpty(binding.edtJobReq) || !isEmpty(binding.textGender)) {
            return false;
        }
        return true;
    }

    private void validate() {
        if (isEmpty(binding.edtNameJob)) {
            binding.layoutErrorName.setVisibility(View.VISIBLE);
            binding.edtNameJob.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorName.setVisibility(View.GONE);
            binding.edtNameJob.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.textPosition)) {
            binding.layoutErrorPosition.setVisibility(View.VISIBLE);
            binding.textPosition.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorPosition.setVisibility(View.GONE);
            binding.textPosition.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.textSalary)) {
            binding.layoutErrorSalary.setVisibility(View.VISIBLE);
            binding.layoutSalary.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorSalary.setVisibility(View.GONE);
            binding.layoutSalary.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.textGender)) {
            binding.layoutErrorGender.setVisibility(View.VISIBLE);
            binding.textGender.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorGender.setVisibility(View.GONE);
            binding.textGender.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.textWorkingForm)) {
            binding.layoutErrorWorkingForm.setVisibility(View.VISIBLE);
            binding.textWorkingForm.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorWorkingForm.setVisibility(View.GONE);
            binding.textWorkingForm.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.textExperience)) {
            binding.layoutErrorExperience.setVisibility(View.VISIBLE);
            binding.textExperience.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorExperience.setVisibility(View.GONE);
            binding.textExperience.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.textTypeJob)) {
            binding.layoutErrorTypeJob.setVisibility(View.VISIBLE);
            binding.textTypeJob.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorTypeJob.setVisibility(View.GONE);
            binding.textTypeJob.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.textApplyAmount)) {
            binding.layoutErrorAmount.setVisibility(View.VISIBLE);
            binding.textApplyAmount.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorAmount.setVisibility(View.GONE);
            binding.textApplyAmount.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.edtDeadline)) {
            binding.layoutErrorDeadline.setVisibility(View.VISIBLE);
            binding.edtDeadline.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorDeadline.setVisibility(View.GONE);
            binding.edtDeadline.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.edtDetailJob)) {
            binding.layoutErrorDescription.setVisibility(View.VISIBLE);
            binding.edtDetailJob.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorDescription.setVisibility(View.GONE);
            binding.edtDetailJob.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }

        if (isEmpty(binding.edtJobReq)) {
            binding.layoutErrorRequire.setVisibility(View.VISIBLE);
            binding.edtJobReq.setBackgroundResource(R.drawable.custom_edittext_cre_news_err);
        } else {
            binding.layoutErrorRequire.setVisibility(View.GONE);
            binding.edtJobReq.setBackgroundResource(R.drawable.custom_edittext_cre_news);
        }
    }

    private boolean isEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty() || editText.getText().toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    private void updateRecruitment(String id, String nameJob, String description, String requirement,
                                   String deadline, String salary, String locationWorking, String idOccupation, String idCompany,
                                   String gender, String experience, String workingForm, String amount) throws JSONException {
        String url = Constant.url_dev + "/job/update";
        loadingDialog.showDialog();
        RequestQueue queue = Volley.newRequestQueue(EditRecruitmentActivity.this);

        boolean isNumberSalary = salary.matches(".*\\d.*");

        JSONObject jsonReq = new JSONObject();

        jsonReq.put("_id", id);
        jsonReq.put("name", nameJob);
        jsonReq.put("description", description);
        jsonReq.put("requirement", requirement);
        jsonReq.put("deadline", deadline);
        jsonReq.put("salary", isNumberSalary == true ? salary + binding.textUnitMoney.getText().toString() : salary);
        jsonReq.put("locationWorking", locationWorking);
        jsonReq.put("idOccupation", idOccupation);
        jsonReq.put("idCompany", idCompany);
        jsonReq.put("gender", gender);
        jsonReq.put("experience", experience);
        jsonReq.put("workingForm", workingForm);
        jsonReq.put("amount", amount);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.PUT, url, jsonReq, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("job");
                    Job job = new Job(
                            jsonObject.getString("_id"),
                            jsonObject.getString("name"),
                            jsonObject.getJSONObject("idCompany").getString("_id"),
                            jsonObject.getJSONObject("idCompany").getString("name"),
                            jsonObject.getString("locationWorking"),
                            jsonObject.getString("salary"),
                            jsonObject.getString("deadline"),
                            jsonObject.getString("description"),
                            jsonObject.getString("requirement"),
                            jsonObject.getJSONObject("idOccupation").getString("_id"),
                            jsonObject.getJSONObject("idOccupation").getString("name"),
                            jsonObject.getJSONObject("idCompany").getString("image"),
                            jsonObject.getString("amount"),
                            jsonObject.getString("workingForm"),
                            jsonObject.getString("experience"),
                            jsonObject.getString("gender")
                    );
                    loadingDialog.hideDialog();
                    sendBroadcast(job);
                    CustomToast.makeText(EditRecruitmentActivity.this, "Cập nhật thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.hideDialog();
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.statusCode == 401 && error.networkResponse.data != null) {
                    Intent i = new Intent(EditRecruitmentActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", preferenceManager.getString(Constant.TOKEN));
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


    private void getTypeJob() {
        String urlTypeJob = Constant.url_dev + "/occupation/list";
        RequestQueue queue = Volley.newRequestQueue(EditRecruitmentActivity.this);

        idTypeJobs = new ArrayList<>();
        nameTypeJobs = new ArrayList<>();
        listSelectedTypeJob = new ArrayList<>();

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, urlTypeJob, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray listTypeJob = response.getJSONObject("data").getJSONArray("data");
                    for (int i = 0; i < listTypeJob.length(); i++) {
                        JSONObject typeJob = listTypeJob.getJSONObject(i);
                        if (typeJob.getString("isDelete").equals("false")) {
                            Log.d("TypeJob", typeJob.getString("name"));
                            nameTypeJobs.add(typeJob.getString("name"));
                            idTypeJobs.add(typeJob.getString("_id"));
                            listSelectedTypeJob.add(false);
                        }
                    }
                    initBottomSheetTypeJob();
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

    private void sendBroadcast(Job job) {
        Intent intent = new Intent(Constant.JOB_MODEL);
        intent.putExtra(Constant.JOB_MODEL, (Serializable) job);
        intent.putExtra(Constant.POSITION, getIntent().getIntExtra(Constant.POSITION, -1));
        sendBroadcast(intent);
    }
}
