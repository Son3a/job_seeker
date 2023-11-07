package com.nsb.job_seeker.fragment.admin;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
import com.nsb.job_seeker.activity.admin.RecruitmentDetailActivity;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.common.PreferenceManager;


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

public class UpdateNewsFragment extends Fragment {
    private View updateNewsView;
    private EditText edtNameJob, edtPlace, edtSalary, edtWorkTime, edtDeadLine,
            edtDetailJob, edtJobReq;
    private TextView tvSaveIdJobs;
    private Spinner spnTypeJob;
    private ImageView icCancel, icCreateRec;
    private TextView tvTimeCreate;
    private ProgressBar loadingPB;
    private String urlCreate = Constant.url_dev + "/job/create";
    private List<String> nameTypeJobs;
    private List<String> idTypeJobs;
    private PreferenceManager preferenceManager;

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
        spnTypeJob = updateNewsView.findViewById(R.id.spn_type_job);
        edtDetailJob = updateNewsView.findViewById(R.id.edt_detail_job);
        edtJobReq = updateNewsView.findViewById(R.id.edt_job_req);
        icCancel = updateNewsView.findViewById(R.id.ic_cancel);
        edtDeadLine = updateNewsView.findViewById(R.id.edt_deadline);
        tvTimeCreate = updateNewsView.findViewById(R.id.tv_time_create_new);
        icCreateRec = updateNewsView.findViewById(R.id.ic_create_recruitment);
        loadingPB = updateNewsView.findViewById(R.id.idLoadingPB);
        tvSaveIdJobs = updateNewsView.findViewById(R.id.tv_save_id_jobs);
        idTypeJobs = new ArrayList<>();
        nameTypeJobs = new ArrayList<>();
        nameTypeJobs.add("Lĩnh vực");

        preferenceManager = new PreferenceManager(getActivity());
    }


    private void setEvent() {

        //set porting date
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date(System.currentTimeMillis());
        tvTimeCreate.setText(formatter.format(date));

        setColorItemSelectedSpinner();

        getTypeJob();

        showFuncCancel();

        pickTime();

        formatBullet();

        clickCreateRecruitment();

        clickCancel();
    }

    private void setColorItemSelectedSpinner(){
        spnTypeJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setTextColor(Color.BLACK); //C
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void pickTime() {
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

                        if (Constant.checkValidDeadline(day, month + 1, year)) {
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
    }

    private void formatBullet() {
        edtDetailJob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                Constant.addNewLine(text, lengthBefore, lengthAfter, edtDetailJob);
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
                Constant.addNewLine(text, lengthBefore, lengthAfter, edtJobReq);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void clickCreateRecruitment() {
        icCreateRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateNotEmpty()) {
                    String nameJob = edtNameJob.getText().toString();
                    String place = edtPlace.getText().toString();
                    String salary = edtSalary.getText().toString();
                    String timeWork = edtWorkTime.getText().toString();
                    String deadLine = edtDeadLine.getText().toString();
                    String descJob = Constant.formatStringFromBullet(edtDetailJob.getText().toString());
                    String jobReq = Constant.formatStringFromBullet(edtJobReq.getText().toString());
                    String idTypeJob = idTypeJobs.get(spnTypeJob.getSelectedItemPosition() - 1);
                    String idCompany = preferenceManager.getString(Constant.COMPANY_ID);


                    try {
                        createRecruitment(nameJob, place, salary, timeWork, deadLine, descJob, jobReq, idTypeJob, idCompany);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(getActivity(), "Bạn chưa điền đủ thông tin !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createRecruitment(String nameJob, String place, String salary, String timeWork,
                                   String deadline, String descJob, String jobReq, String idTypeJob, String idCompany) throws JSONException {
        String access_token = preferenceManager.getString(Constant.TOKEN);
//        loadingPB.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", nameJob);
        jsonObject.put("locationWorking", place);
        jsonObject.put("salary", salary);
        jsonObject.put("hourWorking", timeWork);
        jsonObject.put("deadline", deadline);
        jsonObject.put("description", descJob);
        jsonObject.put("requirement", jobReq);
        jsonObject.put("idOccupation", idTypeJob);
        jsonObject.put("idCompany", idCompany);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, urlCreate, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String id = null;
                try {
                    id = response.getJSONObject("data").getString("_id");
                    Toast.makeText(getActivity(), "Tạo thành công!!!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getActivity(), RecruitmentDetailActivity.class);
                    i.putExtra("id", id);
                    i.putExtra("isChange", "true");
                    startActivity(i);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof com.android.volley.NoConnectionError) {

                } else
                if (error.networkResponse.statusCode == 401 && error.networkResponse.data != null) {
                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
                System.out.println(error);
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


    private boolean validateNotEmpty() {
        if (isEmpty(edtDetailJob) || isEmpty(edtNameJob) ||
                isEmpty(edtPlace) || isEmpty(edtSalary) ||
                isEmpty(edtWorkTime) || isEmpty(edtDeadLine) ||
                isEmpty(edtJobReq) || (spnTypeJob.getSelectedItemPosition() == 0)) {
            return false;
        }
        return true;
    }

    private boolean validateEmpty() {
        if (isEmpty(edtDetailJob) && isEmpty(edtNameJob) &&
                isEmpty(edtPlace) && isEmpty(edtSalary) &&
                isEmpty(edtWorkTime) && isEmpty(edtDeadLine) &&
                isEmpty(edtJobReq) && (spnTypeJob.getSelectedItemPosition() == 0)) {
            return true;
        }
        return false;
    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    private void clickCancel() {
        icCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogCancel("Bạn có muốn hủy quá trình này ?");
            }
        });
    }

    private void showDialogCancel(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
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
                        clearText();
                        clearFocus();
                    }
                }).create();
        dialog.show();
    }

    private void showFuncCancel() {
        edtNameJob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (validateEmpty() && hasFocus) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        edtPlace.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (validateEmpty() && hasFocus) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        edtSalary.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (validateEmpty() && hasFocus) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        edtWorkTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (validateEmpty() && hasFocus) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        spnTypeJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (validateEmpty()) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        edtDetailJob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (validateEmpty() && hasFocus) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        edtJobReq.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (validateEmpty() && hasFocus) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        edtDeadLine.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (validateEmpty() && hasFocus) {
                    icCancel.setVisibility(View.GONE);
                } else {
                    icCancel.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void clearText() {
        edtNameJob.getText().clear();
        edtPlace.getText().clear();
        edtSalary.getText().clear();
        edtWorkTime.getText().clear();
        spnTypeJob.setSelection(0);
        edtDetailJob.getText().clear();
        edtJobReq.getText().clear();
        edtDeadLine.getText().clear();
    }

    private void clearFocus() {
        edtNameJob.clearFocus();
        edtPlace.clearFocus();
        edtSalary.clearFocus();
        edtWorkTime.clearFocus();
        edtDetailJob.clearFocus();
        edtJobReq.clearFocus();
        edtDeadLine.clearFocus();
    }

    private void bindingDataToSpinner() {
        if(getActivity()!=null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.item_spinner, nameTypeJobs);
            spnTypeJob.setAdapter(adapter);
        }
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
                    bindingDataToSpinner();
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
