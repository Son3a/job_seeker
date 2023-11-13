package com.nsb.job_seeker.activity.seeker;

import static java.lang.Math.abs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.admin.EditRecruitmentActivity;
import com.nsb.job_seeker.activity.admin.ListCVActivity;
import com.nsb.job_seeker.activity.messenger.ChatActivity;
import com.nsb.job_seeker.adapter.JobDetailAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivitySeekerJobDetailBinding;
import com.nsb.job_seeker.model.Company;
import com.nsb.job_seeker.model.Job;
import com.nsb.job_seeker.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobDetailActivity extends BaseActivity {
    private ActivitySeekerJobDetailBinding binding;
    private String IDCompany = "";
    private String IDJob = "";
    private JobDetailAdapter jobDetailAdapter;
    private List<Job> listRelatedJob;
    private PreferenceManager preferenceManager;
    private Job job;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.JOB_MODEL.equals(intent.getAction())) {
                Job job1 = (Job) intent.getSerializableExtra(Constant.JOB_MODEL);
                binding.textNameJob.setText(job1.getNameJob());
                binding.textNameCompany.setText(job1.getCompanyName());
                binding.textPosition.setText(job.getPlace());
                binding.textSalary.setText(job.getSalary());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivitySeekerJobDetailBinding.inflate(getLayoutInflater());

        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();
        setEvent();
    }

    private void setControl() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("isLinkCompany")) {
            //binding..setEnabled(false);
        }
        IDJob = bundle.getString(Constant.JOB_ID);
        listRelatedJob = new ArrayList<>();
        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.getString(Constant.ROLE).equals(Constant.ADMIN_ROLE)) {
            binding.tabLayout.setVisibility(View.GONE);
            binding.tabLayout.getLayoutParams().height = 1;
            binding.layoutBottomSheetApply.getRoot().getLayoutParams().height = 0;
        } else {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Thông tin"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công việc liên quan"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công ty"));
        }
    }

    private void setEvent() {
        binding.viewPager.setUserInputEnabled(false);
        getJobDetail();
        setStateAppBar();
        back();
        setTabLayout();
        gotoAppJob();
        clickSaveJob();
        setIconSave();
        openBottomEditRecruitment();
    }

    private void gotoChat(){
        if(job != null){
            binding.layoutBottomSheetApplyAgain.btnMessage.setOnClickListener(v->{
//                User user = new User(
//                        job.getId(),
//                        job
//                );
            });
//        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
//        intent.putExtra()
//        startActivity();
        }
    }

    private void openBottomEditRecruitment() {
        binding.cvExpand.setOnClickListener(v -> {
            if (preferenceManager.getString(Constant.ROLE).equals(Constant.ADMIN_ROLE)) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(JobDetailActivity.this);
                View layoutFunc = getLayoutInflater().inflate(R.layout.layout_function_company, null);
                bottomSheetDialog.setContentView(layoutFunc);

                TextView textTitle = layoutFunc.findViewById(R.id.textOption);
                textTitle.setText("Cập nhật tin tuyển dụng");

                LinearLayout layoutListCv = layoutFunc.findViewById(R.id.layoutListCV);
                layoutListCv.setVisibility(View.VISIBLE);

                layoutListCv.setOnClickListener(view -> {
                    if (!IDJob.equals("")) {
                        Intent intent = new Intent(JobDetailActivity.this, ListCVActivity.class);
                        intent.putExtra(Constant.JOB_ID, IDJob);
                        startActivity(intent);
                        bottomSheetDialog.dismiss();
                    }
                });

                LinearLayout layoutEditCompany = layoutFunc.findViewById(R.id.layoutEditCompany);
                layoutEditCompany.setOnClickListener(view -> {
                    if (job != null) {
                        Intent intent = new Intent(JobDetailActivity.this, EditRecruitmentActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constant.JOB_MODEL, job);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        bottomSheetDialog.dismiss();
                    }
                });
                ImageView imgClose = layoutFunc.findViewById(R.id.imageClose);
                imgClose.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                });

                bottomSheetDialog.show();
            }
        });
    }

    private void setIconSave() {
        if (!IDJob.equals("") && IDJob != null) {
            if (Constant.idSavedJobs.contains(IDJob)) {
                binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_saved);
                binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.green));
            } else {
                binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_not_save);
                binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.secondary_text));
            }
        }
    }

    private void hideLayout(int visible) {
        binding.layoutHeader1.setVisibility(visible);
        binding.tabLayout.setVisibility(visible);
        binding.layoutBottomSheetApply.getRoot().setVisibility(visible);
        binding.viewPager.setVisibility(visible);
    }

    private void gotoAppJob() {
        binding.layoutBottomSheetApply.btnApplyJob.setOnClickListener(v -> {
            Intent intent = new Intent(this, ApplyJobActivity.class);
            intent.putExtra("idJob", IDJob);
            startActivity(intent);
        });

        binding.layoutBottomSheetApplyAgain.btnApplyJob.setOnClickListener(v -> {
            Intent intent = new Intent(this, ApplyJobActivity.class);
            intent.putExtra("idJob", IDJob);
            startActivity(intent);
        });
    }

    private void setStateAppBar() {
        binding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                binding.toolbar.setVisibility(View.VISIBLE);
            } else if (verticalOffset == 0) {

            } else {
                binding.toolbar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setViewAdapter(List<Job> listRelatedJob, Job job, Company company) {
        jobDetailAdapter = new JobDetailAdapter(JobDetailActivity.this, listRelatedJob, job, company);
        binding.viewPager.setAdapter(jobDetailAdapter);
    }

    private void setTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void back() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void clickSaveJob() {
        binding.layoutBottomSheetApply.cvSaveJob.setOnClickListener(v -> {
            try {
                saveJob(IDJob);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void saveJob(String jobId) throws JSONException {
        String base_url = Constant.url_dev + "/job";

        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
//        pbLoading.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");

                    if (status.equals("1")) {
                        binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_saved);
                        binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.green));
                        Constant.idSavedJobs.add(jobId);
                    } else {
                        binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_not_save);
                        Constant.idSavedJobs.remove(Constant.idSavedJobs.size() - 1);
                        binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.secondary_text));
                        CustomToast.makeText(JobDetailActivity.this, "Bạn đã bỏ lưu công việc!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(JobDetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    pbLoading.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.data != null) {
                    try {
                        if (error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(JobDetailActivity.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        Toast.makeText(JobDetailActivity.this, message.substring(1, message.length() - 1), Toast.LENGTH_SHORT).show();
                        //pbLoading.setVisibility(View.GONE);
                        Log.d("ABC", body);
                    } catch (UnsupportedEncodingException e) {
                        //pbLoading.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
                return params;
            }

            ;
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
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
        mRequestQueue.add(jsonObjectRequest);
    }

    private void getJobDetail() {
        RequestQueue requestQueue = Volley.newRequestQueue(JobDetailActivity.this);

        String url = Constant.url_dev + "/job/detail?id=" + IDJob;
//        binding.idLoadingPB.setVisibility(View.VISIBLE);
        hideLayout(View.INVISIBLE);

        JsonObjectRequest data = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("data");

                    String nameCompany = "", place = "";
                    if (!jsonObject.isNull("idCompany")) {
                        nameCompany = jsonObject.getJSONObject("idCompany").getString("name");
                        place = jsonObject.getJSONObject("idCompany").getString("location");
                        IDCompany = jsonObject.getJSONObject("idCompany").getString("_id");
                    }

                    binding.textNameJob.setText(jsonObject.getString("name"));
                    binding.textNameCompany.setText(nameCompany);
                    binding.textPosition.setText(place);
                    binding.textSalary.setText(jsonObject.getString("salary"));
                    if (!jsonObject.getJSONObject("idCompany").getString("image").isEmpty()) {
                        binding.imageCompany.setImageBitmap(Constant.getBitmapFromEncodedString(jsonObject.getJSONObject("idCompany").getString("image")));
                    }

                    for (int i = 0; i < jsonObject.getJSONArray("relatedJob").length(); i++) {
                        JSONObject jobRelated = jsonObject.getJSONArray("relatedJob").getJSONObject(i);

                        if (jobRelated.getString("status").equals("true")) {
                            listRelatedJob.add(new Job(
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
                            ));
                        }
                    }

                    Company company = new Company(
                            jsonObject.getJSONObject("idCompany").getString("_id"),
                            jsonObject.getJSONObject("idCompany").getString("name"),
                            jsonObject.getJSONObject("idCompany").getString("isDelete"),
                            jsonObject.getJSONObject("idCompany").getString("link"),
                            jsonObject.getJSONObject("idCompany").getString("image"),
                            jsonObject.getJSONObject("idCompany").getInt("totalEmployee"),
                            jsonObject.getJSONObject("idCompany").getString("about"),
                            jsonObject.getJSONObject("idCompany").getString("address"),
                            jsonObject.getJSONObject("idCompany").getString("location"),
                            jsonObject.getJSONObject("idCompany").getString("phone")
                    );

                    job = new Job(
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

                    setViewAdapter(
                            listRelatedJob,
                            job,
                            company
                    );

                    if (Constant.idAppliedJob.contains(jsonObject.getString("_id"))) {
                        binding.layoutBottomSheetApply.getRoot().setVisibility(View.INVISIBLE);
                        binding.layoutBottomSheetApplyAgain.getRoot().setVisibility(View.VISIBLE);
                    } else {
                        binding.layoutBottomSheetApplyAgain.getRoot().setVisibility(View.INVISIBLE);
                        binding.layoutBottomSheetApply.getRoot().setVisibility(View.VISIBLE);
                    }
                    hideLayout(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Error", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof com.android.volley.NoConnectionError) {

                }
                Log.d("Error", error.getMessage());
                ;
            }
        });
        data.setRetryPolicy(new RetryPolicy() {
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
        requestQueue.add(data);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Constant.JOB_MODEL);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
