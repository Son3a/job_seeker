package com.nsb.job_seeker.fragment.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentEmployerRecruitmentsBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class RecruitmentsFragment extends Fragment implements JobListener {
    private FragmentEmployerRecruitmentsBinding binding;
    private PreferenceManager preferenceManager;
    private List<Job> jobList;
    private JobAdapter jobAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployerRecruitmentsBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(getContext(), jobList, this, false);
        binding.rcvRecruitments.setAdapter(jobAdapter);

        preferenceManager = new PreferenceManager(getActivity());
    }


    private void setEvent() {
        getListJobOfCompany();
        refreshContent();
    }

    private void refreshContent() {
        binding.layoutRefresh.setOnRefreshListener(() -> {
            binding.layoutRefresh.setRefreshing(false);
            getListJobOfCompany();
        });
    }

    private void getListJobOfCompany() {
        String url = Constant.url_dev + "/job/list/company/" + preferenceManager.getString(Constant.COMPANY_ID);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        binding.idLoadingPB.setVisibility(View.VISIBLE);
        jobList.clear();
        jobAdapter.notifyDataSetChanged();
        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jobsList = response.getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (job.getString("status").equals("true")) {
                                    jobList.add(new Job(
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
                                            job.getString("workingForm"),
                                            job.getString("experience"),
                                            job.getString("gender")
                                    ));
                                }
                            }
                            binding.idLoadingPB.setVisibility(View.GONE);
                            binding.textAmount.setText(String.valueOf(jobsList.length()));
                            jobAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof com.android.volley.NoConnectionError) {

                        }
                        System.out.println(error);
                    }
                }
        );
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
        queue.add(data);
    }

    @Override
    public void onClick(Job job) {
        Intent intent = new Intent(getContext(), JobDetailActivity.class);
        intent.putExtra(Constant.JOB_ID, job.getId());
        startActivity(intent);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding) {

    }
}
