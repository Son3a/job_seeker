package com.nsb.job_seeker.fragment.seeker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentSeekerSavedJobBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyJobSavedFragment extends Fragment implements JobListener {
    private FragmentSeekerSavedJobBinding binding;
    private List<Job> listSavedJob;
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
        if (listSavedJob == null) {
            listSavedJob = new ArrayList<>();
        }
        preferenceManager = new PreferenceManager(getActivity());
        jobAdapter = new JobAdapter(getContext(), listSavedJob, this, true);
        binding.lvJobSaved.setAdapter(jobAdapter);
        loadingDialog = new LoadingDialog(getContext());
    }

    private void setEvent() {
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            getJobSaved();
            refreshContent();
        } else {
            gotoLogin();
            binding.layoutRefresh.setEnabled(false);
            binding.layoutLogin.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.lvJobSaved.setVisibility(View.GONE);
        }

    }

    private void gotoLogin() {
        binding.btnLoginSave.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
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
        listSavedJob.clear();
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
                                    listSavedJob.add(new Job(
                                            job.getString("_id"),
                                            job.getString("name"),
                                            job.getJSONObject("idCompany").getString("_id"),
                                            job.getJSONObject("idCompany").getString("name"),
                                            job.getString("locationWorking"),
                                            job.getString("salary"),
                                            job.getString("deadline"),
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
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", error.toString());
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

    private void saveJob(String jobId, ListViewItemJobBinding itemJobBinding, int position) throws JSONException {
        String base_url = Constant.url_dev + "/job";
        RequestQueue mRequestQueue = Volley.newRequestQueue(getContext());
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
        loadingDialog.showDialog();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    loadingDialog.hideDialog();
                    String status = response.getString("status");

                    if (status.equals("1")) {
                        Constant.idSavedJobs.add(jobId);
                        CustomToast.makeText(getContext(), "Lưu việc làm thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    } else {
                        Constant.idSavedJobs.remove(jobId);
                        listSavedJob.remove(position);
                        jobAdapter.notifyItemRemoved(position);
                        jobAdapter.notifyItemRangeChanged(position, listSavedJob.size());
                        Log.d("PositionSave", position + "");
                        if (listSavedJob.size() == 0) {
                            binding.layoutEmpty.setVisibility(View.VISIBLE);
                        }
                        CustomToast.makeText(getContext(), "Bạn đã bỏ lưu công việc!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                    pbLoading.setVisibility(View.GONE);
                    loadingDialog.hideDialog();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                loadingDialog.hideDialog();
                //get status code here
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.data != null) {
                    try {
                        if (error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(getContext(), LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        Toast.makeText(getActivity(), message.substring(1, message.length() - 1), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(Job job, int position) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra(Constant.JOB_ID, job.getId());
        i.putExtra("isApply", true);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding, int position) {
        try {
            saveJob(job.getId(), binding, position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

