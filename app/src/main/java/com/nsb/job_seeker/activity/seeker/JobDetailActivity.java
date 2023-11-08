package com.nsb.job_seeker.activity.seeker;

import static java.lang.Math.abs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.adapter.JobDetailAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivitySeekerJobDetailBinding;
import com.nsb.job_seeker.model.Company;
import com.nsb.job_seeker.model.Job;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
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
        IDJob = bundle.getString("id");
        listRelatedJob = new ArrayList<>();
        preferenceManager = new PreferenceManager(this);
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Thông tin"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công việc liên quan"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công ty"));
    }


    private void setEvent() {
        binding.viewPager.setUserInputEnabled(false);

        getJobDetail();
        setStateAppBar();
        back();
        setTabLayout();
        changeBtnSubmit();
        applyJob();
        gotoAppJob();
        clickSaveJob();
        setIconSave();
    }

    private void setIconSave() {
        if (!IDJob.equals("")) {
            if (Constant.idSavedJobs.contains(IDJob)) {
                binding.layoutBottomSheet.imageSave.setImageResource(R.drawable.ic_saved);
                binding.layoutBottomSheet.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.green));
            } else {
                binding.layoutBottomSheet.imageSave.setImageResource(R.drawable.ic_not_save);
                binding.layoutBottomSheet.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.secondary_text));
            }
        }
    }

    private void hideLayout(int visible) {
        binding.layoutHeader1.setVisibility(visible);
        binding.tabLayout.setVisibility(visible);
        binding.layoutBottomSheet.getRoot().setVisibility(visible);
        binding.viewPager.setVisibility(visible);
    }

    private void gotoAppJob() {
        binding.layoutBottomSheet.btnApplyJob.setOnClickListener(v -> {
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

    private void applyJob() {
//        btnApply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
//                i.putExtra("idJob",IDJob);
//                startActivity(i);
//            }
//        });
    }

    private void changeBtnSubmit() {
//        Bundle bundle = getIntent().getExtras();
//        if (bundle.containsKey("isApply")) {
//            boolean isApply = bundle.getBoolean("isApply");
//            if (!isApply) {
//                binding.layoutBottomSheet.btnApplyJob.setText("Đã ứng tuyển");
//                //btnApply.setEnabled(false);
//            } else {
//                binding.layoutBottomSheet.btnApplyJob.setText("ỨNG TUYỂN NGAY");
//            }
//        } else {
//            binding.layoutBottomSheet.btnApplyJob.setVisibility(View.GONE);
//        }
    }

    private void clickSaveJob() {
        binding.layoutBottomSheet.cvSaveJob.setOnClickListener(v -> {
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
                        binding.layoutBottomSheet.imageSave.setImageResource(R.drawable.ic_saved);
                        binding.layoutBottomSheet.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.green));
                        Constant.idSavedJobs.add(jobId);
                    } else {
                        binding.layoutBottomSheet.imageSave.setImageResource(R.drawable.ic_not_save);
                        Constant.idSavedJobs.remove(Constant.idSavedJobs.size() - 1);
                        binding.layoutBottomSheet.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.secondary_text));
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
                    JSONObject job = response.getJSONObject("data");

                    String nameCompany = "", place = "";
                    if (!job.isNull("idCompany")) {
                        nameCompany = job.getJSONObject("idCompany").getString("name");
                        place = job.getJSONObject("idCompany").getString("location");
                        IDCompany = job.getJSONObject("idCompany").getString("_id");
                    }

                    binding.textNameJob.setText(job.getString("name"));
                    binding.textNameCompany.setText(nameCompany);
                    binding.textPosition.setText(place);
                    binding.textSalary.setText(job.getString("salary"));
                    Picasso.get().load(job.getJSONObject("idCompany").getString("image")).into(binding.imageCompany);

                    for (int i = 0; i < job.getJSONArray("relatedJob").length(); i++) {
                        JSONObject jobRelated = job.getJSONArray("relatedJob").getJSONObject(i);

                        if (jobRelated.getString("status").equals("true")) {
                            listRelatedJob.add(new Job(
                                    job.getString("_id"),
                                    job.getString("name"),
                                    job.getJSONObject("idCompany").getString("name"),
                                    job.getString("locationWorking"),
                                    job.getString("salary"),
                                    Constant.setTime(job.getString("deadline")),
                                    job.getString("description"),
                                    job.getString("requirement"),
                                    job.getJSONObject("idOccupation").getString("name"),
                                    job.getJSONObject("idCompany").getString("image"),
                                    job.getString("amount"),
                                    job.getString("working_form"),
                                    job.getString("experience"),
                                    job.getString("gender")
                            ));
                        }
                    }

                    Company company = new Company(
                            job.getJSONObject("idCompany").getString("_id"),
                            job.getJSONObject("idCompany").getString("name"),
                            job.getJSONObject("idCompany").getString("isDelete"),
                            job.getJSONObject("idCompany").getString("link"),
                            job.getJSONObject("idCompany").getString("image"),
                            job.getJSONObject("idCompany").getString("totalEmployee"),
                            job.getJSONObject("idCompany").getString("about"),
                            job.getJSONObject("idCompany").getString("address"),
                            job.getJSONObject("idCompany").getString("location"),
                            job.getJSONObject("idCompany").getString("idUser"),
                            job.getJSONObject("idCompany").getString("phone")
                    );

                    setViewAdapter(
                            listRelatedJob,
                            new Job(
                                    job.getString("_id"),
                                    job.getString("name"),
                                    job.getJSONObject("idCompany").getString("name"),
                                    job.getString("locationWorking"),
                                    job.getString("salary"),
                                    Constant.setTime(job.getString("deadline")),
                                    job.getString("description"),
                                    job.getString("requirement"),
                                    job.getJSONObject("idOccupation").getString("name"),
                                    job.getJSONObject("idCompany").getString("image"),
                                    job.getString("amount"),
                                    job.getString("working_form"),
                                    job.getString("experience"),
                                    job.getString("gender")
                            ),
                            company
                    );

                    if(Constant.idAppliedJob.contains(job.getString("_id"))){
                        binding.layoutBottomSheet.getRoot().setVisibility(View.INVISIBLE);
                        binding.layoutBottomSheetApplyAgain.getRoot().setVisibility(View.VISIBLE);
                    } else {
                        binding.layoutBottomSheetApplyAgain.getRoot().setVisibility(View.INVISIBLE);
                        binding.layoutBottomSheet.getRoot().setVisibility(View.VISIBLE);
                    }
                    hideLayout(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Error", e.getMessage());
                } catch (ParseException e) {
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
}
