package com.nsb.job_seeker.fragment.seeker;

import android.content.Intent;
import android.os.Bundle;
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
import com.nsb.job_seeker.databinding.FragmentSeekerAppliedJobBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyJobAppliedFragment extends Fragment implements JobListener {
    private FragmentSeekerAppliedJobBinding binding;
    private List<Job> jobListApplied;
    private PreferenceManager preferenceManager;
    private JobAdapter jobAdapter;
    private LoadingDialog loadingDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeekerAppliedJobBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
        loadingDialog = new LoadingDialog(getContext());
        jobListApplied = new ArrayList<>();
        preferenceManager = new PreferenceManager(getActivity());
        jobAdapter = new JobAdapter(getContext(), jobListApplied, this, false);
        binding.lvJobApplied.setAdapter(jobAdapter);
    }

    private void setEvent() {
        getJobApplied();
        refreshContent();
    }

    private void refreshContent() {
        binding.layoutRefresh.setOnRefreshListener(() -> {
            binding.layoutRefresh.setRefreshing(false);
            getJobApplied();
        });
    }

    private void getJobApplied() {
        String url = Constant.url_dev + "/application/get-by-userid";
        String token = preferenceManager.getString(Constant.TOKEN);
        jobListApplied.clear();
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        loadingDialog.showDialog();
        JsonObjectRequest data = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jobsList = response.getJSONObject("data").getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i).getJSONObject("idJob");
                                jobListApplied.add(new Job(
                                        job.getString("_id"),
                                        job.getString("name"),
                                        job.getJSONObject("idCompany").getString("name"),
                                        job.getString("locationWorking"),
                                        job.getString("salary"),
                                        Constant.setTime(job.getString("deadline")),
                                        job.getString("description"),
                                        job.getString("requirement"),
                                        "",
                                        job.getJSONObject("idCompany").getString("image"),
                                        job.getString("amount"),
                                        job.getString("working_form"),
                                        job.getString("experience"),
                                        job.getString("gender")
                                ));

                            }
//                            if (jobsList.length() == 0) {
//                                tvNotify.setVisibility(View.VISIBLE);
//                            }
                            binding.layoutRefresh.setRefreshing(false);
                            jobAdapter.notifyDataSetChanged();
                            loadingDialog.hideDialog();
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

                        } else if (error.networkResponse.statusCode == 401 && error.networkResponse.data != null) {
                            Intent i = new Intent(getActivity(), LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        System.out.println(error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
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
                if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                    Intent i = new Intent(getContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
            }
        });
        queue.add(data);
    }

    private String formatTimeApply(String timeApply) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("HH:mm dd-MM-yyyy");

        String create = output.format(sdf.parse(timeApply));
        System.out.println(create);
        return create;
    }

    @Override
    public void onClick(Job job) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", false);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding) {

    }
}
