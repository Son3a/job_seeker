package com.nsb.job_seeker.fragment.admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecruitmentsFragment extends Fragment implements JobListener {
    private FragmentEmployerRecruitmentsBinding binding;
    private PreferenceManager preferenceManager;
    private List<Job> jobList;
    private JobAdapter jobAdapter;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.JOB_MODEL.equals(intent.getAction())) {
                int position = intent.getIntExtra(Constant.POSITION, -1);
                Job job = (Job) intent.getSerializableExtra(Constant.JOB_MODEL);
                jobList.get(position).setId(job.getId());
                jobList.get(position).setId(job.getNameJob());
                jobList.get(position).setId(job.getCompanyId());
                jobList.get(position).setId(job.getCompanyName());
                jobList.get(position).setId(job.getPlace());
                jobList.get(position).setId(job.getSalary());
                jobList.get(position).setId(job.getDeadline());
                jobList.get(position).setId(job.getDesJob());
                jobList.get(position).setId(job.getReqJob());
                jobList.get(position).setId(job.getTypeId());
                jobList.get(position).setId(job.getTypeJob());
                jobList.get(position).setId(job.getImage());
                jobList.get(position).setId(job.getAmountRecruitment());
                jobList.get(position).setId(job.getWorkingForm());
                jobList.get(position).setId(job.getExperience());
                jobList.get(position).setId(job.getGender());

                jobAdapter.notifyItemChanged(position);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployerRecruitmentsBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
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

    private void setAdapter(){
        jobAdapter = new JobAdapter(getContext(), jobList, this, false);
        binding.rcvRecruitments.setAdapter(jobAdapter);
    }

    private void getListJobOfCompany() {
        String url = Constant.url_dev + "/job/list/all-moderator-job";
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        binding.idLoadingPB.setVisibility(View.VISIBLE);
        jobList = new ArrayList<>();
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
                                            job.getJSONObject("idCompany").getString("_id"),
                                            job.getJSONObject("idCompany").getString("name"),
                                            job.getString("locationWorking"),
                                            job.getString("salary"),
                                            job.getString("deadline"),
                                            job.getString("description"),
                                            job.getString("requirement"),
                                            job.getJSONObject("idOccupation").getString("_id"),
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

                            setAdapter();
                        } catch (JSONException e) {
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", preferenceManager.getString(Constant.TOKEN));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
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
    public void onClick(Job job, int position) {
        Intent intent = new Intent(getContext(), JobDetailActivity.class);
        intent.putExtra(Constant.JOB_ID, job.getId());
        intent.putExtra(Constant.POSITION, position);
        startActivity(intent);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding, int position) {

    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Constant.JOB_MODEL);
        getContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(broadcastReceiver);
    }
}
