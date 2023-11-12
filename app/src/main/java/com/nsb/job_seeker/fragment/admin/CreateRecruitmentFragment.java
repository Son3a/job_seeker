package com.nsb.job_seeker.fragment.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.adapter.FilterAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomDialogDelete;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentEmployerUpdateNewsBinding;
import com.nsb.job_seeker.listener.FilterListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateRecruitmentFragment extends Fragment {
    private FragmentEmployerUpdateNewsBinding binding;
    private String urlCreate = Constant.url_dev + "/job/create";
    private List<String> nameTypeJobs;
    private List<String> idTypeJobs;
    private PreferenceManager preferenceManager;
    private BottomSheetDialog bottomSheetUnitMoney, bottomSheetTypeJob, bottomSheetGender;
    private String idOccupation;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployerUpdateNewsBinding.inflate(getLayoutInflater());
        setControl();
        setEvent();
        return binding.getRoot();
    }

    private void setControl() {
        idTypeJobs = new ArrayList<>();
        nameTypeJobs = new ArrayList<>();
        nameTypeJobs.add("Lĩnh vực");

        loadingDialog = new LoadingDialog(getContext());
        preferenceManager = new PreferenceManager(getActivity());
    }


    private void setEvent() {

        //set porting date
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date(System.currentTimeMillis());

        pickTime();

        formatBullet();

        openBottomUnitMoney();

        openBottomSheetTypeJob();

        openBottomSheetGender();

        clickCreateJob();

        cancelProcess();
    }

    private void cancelProcess() {
        binding.btnCancel.setOnClickListener(v -> {
            if (checkAllEmpty()) {
                return;
            }
            CustomDialogDelete customDialogDelete = new CustomDialogDelete(getContext(), "Có", "Không", false) {
                @Override
                public void doSomeThing() {
                    clearText();
                }
            };
            customDialogDelete.openDiaLogDelete("Bạn chắc chắn muốn hủy quá trình này?");
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
            getTypeJob();
        });
    }

    private void openBottomSheetGender() {
        binding.textGender.setOnClickListener(v -> {
            bottomSheetGender = new BottomSheetDialog(getContext());
            View layoutGender = getLayoutInflater().inflate(R.layout.layout_filter, null);
            TextView textTitle = (TextView) layoutGender.findViewById(R.id.textTitle);
            textTitle.setText("Chọn giới tính");
            List<String> listGender = new ArrayList<>();
            listGender.add("Không yêu cầu");
            listGender.add("Nam");
            listGender.add("Nữ");
            RecyclerView recyclerView = layoutGender.findViewById(R.id.rcvFilter);
            FilterAdapter positionAdapter = new FilterAdapter(listGender, new FilterListener() {
                @Override
                public void onClickItem(String data, int position) {
                    binding.textGender.setText(data);
                    bottomSheetGender.dismiss();
                }
            });
            recyclerView.setAdapter(positionAdapter);
            bottomSheetGender.setContentView(layoutGender);
            bottomSheetGender.show();

            ImageView imgClose = layoutGender.findViewById(R.id.imageClose);
            imgClose.setOnClickListener(view -> {
                bottomSheetGender.dismiss();
            });
        });
    }

    private void initBottomSheetTypeJob(List<String> listTypeJob) {
        bottomSheetTypeJob = new BottomSheetDialog(getContext());
        View layoutTypeJob = getLayoutInflater().inflate(R.layout.layout_filter, null);
        TextView textTitle = (TextView) layoutTypeJob.findViewById(R.id.textTitle);
        textTitle.setText("Chọn loại công việc");

        RecyclerView recyclerView = layoutTypeJob.findViewById(R.id.rcvFilter);
        FilterAdapter filterAdapter = new FilterAdapter(listTypeJob, new FilterListener() {
            @Override
            public void onClickItem(String data, int position) {
                binding.textTypeJob.setText(data);
                bottomSheetTypeJob.dismiss();
            }
        });
        recyclerView.setAdapter(filterAdapter);
        bottomSheetTypeJob.setContentView(layoutTypeJob);
        bottomSheetTypeJob.show();

        ImageView imgClose = layoutTypeJob.findViewById(R.id.imageClose);
        imgClose.setOnClickListener(view -> {
            bottomSheetTypeJob.dismiss();
        });
    }

    private void openBottomUnitMoney() {
        binding.textUnitMoney.setOnClickListener(v -> {
            bottomSheetUnitMoney = new BottomSheetDialog(getContext());
            View layoutUnitMoney = getLayoutInflater().inflate(R.layout.layout_filter, null);
            TextView textTitle = (TextView) layoutUnitMoney.findViewById(R.id.textTitle);
            textTitle.setText("Chọn đơn vị tiền");
            List<String> listUnitMoney = new ArrayList<>();
            listUnitMoney.add("VND");
            listUnitMoney.add("USD");
            RecyclerView recyclerView = layoutUnitMoney.findViewById(R.id.rcvFilter);
            FilterAdapter filterAdapter = new FilterAdapter(listUnitMoney, new FilterListener() {
                @Override
                public void onClickItem(String data, int position) {
                    binding.textUnitMoney.setText(data);
                    bottomSheetUnitMoney.dismiss();
                }
            });
            recyclerView.setAdapter(filterAdapter);
            bottomSheetUnitMoney.setContentView(layoutUnitMoney);
            bottomSheetUnitMoney.show();

            ImageView imgClose = layoutUnitMoney.findViewById(R.id.imageClose);
            imgClose.setOnClickListener(view -> {
                bottomSheetUnitMoney.dismiss();
            });
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        if (Constant.checkValidDeadline(day, month + 1, year)) {
                            binding.edtDeadline.setText((month + 1) + "-" + day + "-" + year);
                        } else {
                            Toast.makeText(getActivity(), "Hạn nộp không hợp lệ, vui lòng chọn lại !", Toast.LENGTH_SHORT).show();
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

    private void clickCreateJob() {
        binding.btnSave.setOnClickListener(v -> {
            validate();
            try {
                if (!checkExistEmpty()) {
                    createRecruitment(
                            binding.edtNameJob.getText().toString().trim(),
                            Constant.formatStringToBullet(binding.edtDetailJob.getText().toString().trim()),
                            Constant.formatStringToBullet(binding.edtJobReq.getText().toString().trim()),
                            binding.edtDeadline.getText().toString().trim(),
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

    private void createRecruitment(String nameJob, String description, String requirement,
                                   String deadline, String salary, String locationWorking, String idOccupation, String idCompany,
                                   String gender, String experience, String workingForm, String amount) throws JSONException {
        String access_token = preferenceManager.getString(Constant.TOKEN);
        loadingDialog.showDialog();
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", nameJob);
        jsonObject.put("description", description);
        jsonObject.put("requirement", requirement);
        jsonObject.put("deadline", deadline);
        jsonObject.put("salary", salary);
        jsonObject.put("locationWorking", locationWorking);
        jsonObject.put("idOccupation", idOccupation);
        jsonObject.put("idCompany", idCompany);
        jsonObject.put("gender", gender);
        jsonObject.put("experience", experience);
        jsonObject.put("workingForm", workingForm);
        jsonObject.put("amount", amount);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, urlCreate, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String id = null;
                try {
                    loadingDialog.hideDialog();
                    id = response.getJSONObject("data").getString("_id");
                    CustomToast.makeText(getActivity(), "Tạo tin tuyển dụng thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    Intent i = new Intent(getActivity(), JobDetailActivity.class);
                    i.putExtra(Constant.JOB_ID, id);
                    i.putExtra("isChange", "true");
                    startActivity(i);

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
                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", access_token);
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
        RequestQueue queue = Volley.newRequestQueue(getActivity());

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
                        }
                    }
                    initBottomSheetTypeJob(nameTypeJobs);
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
