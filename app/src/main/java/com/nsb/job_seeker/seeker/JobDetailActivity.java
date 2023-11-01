package com.nsb.job_seeker.seeker;

import static java.lang.Math.abs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.adapter.JobDetailAdapter;
import com.nsb.job_seeker.databinding.ActivitySeekerJobDetailBinding;
import com.nsb.job_seeker.model.Company;
import com.nsb.job_seeker.model.Job;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class JobDetailActivity extends AppCompatActivity {
    private ActivitySeekerJobDetailBinding binding;
    private String IDCompany = "";
    private String IDJob = "";
    private JobDetailAdapter jobDetailAdapter;
    private List<Job> listRelatedJob;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivitySeekerJobDetailBinding.inflate(getLayoutInflater());

        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();


        getJobDetail();
        setEvent();
    }

    private void setControl() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("isLinkCompany")) {
            //binding..setEnabled(false);
        }
        IDJob = bundle.getString("id");
        listRelatedJob = new ArrayList<>();
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Thông tin"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công việc liên quan"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công ty"));
    }


    private void setEvent() {
        binding.viewPager.setUserInputEnabled(false);

        setStateAppBar();

        back();

        setTabLayout();

        changeBtnSubmit();

        applyJob();

        gotoAppJob();
    }

    private void gotoAppJob() {
        binding.layoutBottomSheet.btnApplyJob.setOnClickListener(v -> {
            Intent intent = new Intent(this, ApplyJobActivity.class);
            intent.putExtra("idJob",IDJob);
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

    private void getJobDetail() {
        RequestQueue requestQueue = Volley.newRequestQueue(JobDetailActivity.this);

        String url = Program.url_dev + "/job/detail?id=" + IDJob;
//        binding.idLoadingPB.setVisibility(View.VISIBLE);

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
                                    Program.setTime(job.getString("deadline")),
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
                                    Program.setTime(job.getString("deadline")),
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

                    //setContentView(R.layout.activity_seeker_job_detail);

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
                Log.d("Error", error.getMessage());;
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

            }
        });
        requestQueue.add(data);
    }
}
