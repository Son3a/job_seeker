package com.nsb.job_seeker.fragment.seeker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentSeekerSavedJobBinding;
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

public class MyJobSavedFragment extends Fragment implements JobListener {
    private FragmentSeekerSavedJobBinding binding;
    private List<Job> jobList;
    private PreferenceManager preferenceManager;
    private JobAdapter jobAdapter;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeekerSavedJobBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();
        return binding.getRoot();
    }

    private void setControl() {
        jobList = new ArrayList<>();
        preferenceManager = new PreferenceManager(getActivity());

        jobAdapter = new JobAdapter(getContext(), jobList, this, true);
        binding.lvJobSaved.setAdapter(jobAdapter);
        loadingDialog = new LoadingDialog(getContext());
    }

    private void setEvent() {
        getJobSaved();
        refreshContent();
    }

    private void refreshContent() {
        binding.layoutRefresh.setOnRefreshListener(() -> {
            binding.layoutRefresh.setRefreshing(false);
            getJobSaved();
        });
    }

    private void getJobSaved() {
        String url = Constant.url_dev + "/auth/info-user";
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        jobList.clear();
        binding.layoutEmpty.setVisibility(View.GONE);
        loadingDialog.showDialog();
        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jobsList = response.getJSONArray("jobFavourite");
                            for (int i = 0; i < jobsList.length(); i++) {
                                if (jobsList.getJSONObject(i).isNull("jobId")) break;
                                JSONObject job = jobsList.getJSONObject(i).getJSONObject("jobId");
                                if (job.getString("status").equals("true")) {
                                    Log.d("Job", job.getString("_id"));
                                    jobList.add(new Job(
                                            job.getString("_id"),
                                            job.getString("name"),
                                            job.getJSONObject("idCompany").getString("_id"),
                                            job.getJSONObject("idCompany").getString("name"),
                                            job.getString("locationWorking"),
                                            job.getString("salary"),
                                            Constant.setTime(job.getString("deadline")),
                                            job.getString("description"),
                                            job.getString("requirement"),
                                            "",
                                            "",
                                            job.getJSONObject("idCompany").getString("image"),
                                            job.getString("amount"),
                                            job.getString("workingForm"),
                                            job.getString("experience"),
                                            job.getString("gender")
                                    ));
                                }
                            }

                            loadingDialog.hideDialog();
                            jobAdapter.notifyDataSetChanged();
                            if (jobsList.length() == 0) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                            }
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
                        Log.d("Error", error.getMessage());
                        if (error instanceof com.android.volley.NoConnectionError) {

                        } else if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(getActivity(), LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
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
    public void onClick(Job job) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", true);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding) {

    }
}

