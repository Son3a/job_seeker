package com.nsb.job_seeker.seeker;

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
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.auth.LoginActivity;
import com.nsb.job_seeker.common.AsyncTasks;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentSeekerForMeBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForMeFragment extends Fragment implements JobListener {
    FragmentSeekerForMeBinding binding;
    private String url = Program.url_dev + "/job/list/sort-by-date";
    private PreferenceManager preferenceManager;
    private JobAdapter jobAdapter;

    public static List<Job> jobList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeekerForMeBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
        jobList = new ArrayList<Job>();

        preferenceManager = new PreferenceManager(getActivity());
        jobAdapter = new JobAdapter(getContext(), jobList, this, true);
        //   PickerLayoutManager pickerLayoutManager = new PickerLayoutManager();
        binding.lvJob.setClipChildren(false);

        binding.lvJob.setAdapter(jobAdapter);
    }

    private void setEvent() {
        getNewJobs(url);
        setStateAppBar();
        gotoSearch();
    }

    private void gotoSearch() {
        binding.includeSearch.layoutSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });

        binding.includeSearch1.layoutSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });
    }

    private void getNewJobs(String url) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
//        List<String> listSavedJob = preferenceManager.getArray(Program.LIST_SAVED_JOB);
//        Log.d("itemSave", listSavedJob.toString());

        //pbLoading.setVisibility(View.VISIBLE);
        JsonObjectRequest data = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String idCompany = "";
                            JSONArray jobsList = response.getJSONArray("data");
//                            int lenghJobs = jobsList.length();
//                            if (Program.calculateTime(jobsList.getJSONObject(0).getString("updateDate")) >= (7 * 24 * 60 * 60 * 1000)) {      //check time update job if it > 1 week to show on news
//                                lenghJobs = 10;
//                            }
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (job.getString("status").equals("true")) {
                                    jobList.add(new Job(
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
                            jobAdapter.notifyDataSetChanged();
                            //pbLoading.setVisibility(View.GONE);

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

            }
        });
        queue.add(data);
    }

    private void setStateAppBar() {
//        appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
//            if (abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
//                toolbar.setVisibility(View.VISIBLE);
//            } else if (verticalOffset == 0) {
//
//            } else {
//                toolbar.setVisibility(View.INVISIBLE);
//            }
//        });

        binding.layoutNested.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int height = binding.layoutHeader.getHeight();
                if (scrollY >= height) {
                    binding.toolBar.setVisibility(View.VISIBLE);
                    Log.d("Position", scrollX + "  " + scrollY + "  " + height);
                } else {
                    binding.toolBar.setVisibility(View.GONE);
                }

            }
        });
    }

    private void saveJob(String jobId, int position, ListViewItemJobBinding itemJobBinding) throws JSONException {
        String base_url = Program.url_dev + "/job";
        itemJobBinding.layoutItemJob.animate()
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        itemJobBinding.layoutItemJob.setVisibility(View.INVISIBLE);
                    }
                });

        itemJobBinding.layoutItemJob.setVisibility(View.INVISIBLE);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getContext());
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
//        pbLoading.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    String status = response.getString("status");

                    if (status.equals("1")) {
                        itemJobBinding.imgSaveJob.setImageResource(R.drawable.ic_saved);
                        itemJobBinding.imgSaveJob.setColorFilter(ContextCompat.getColor(getContext(), R.color.green));
                        Program.idSavedJobs.add(jobId);
                    } else {
                        itemJobBinding.imgSaveJob.setImageResource(R.drawable.ic_not_save);
                        Program.idSavedJobs.remove(Program.idSavedJobs.size() - 1);
                        itemJobBinding.imgSaveJob.setColorFilter(ContextCompat.getColor(getContext(), R.color.secondary_text));
                    }

                    itemJobBinding.layoutItemJob.setVisibility(View.VISIBLE);
//                    }


                } catch (JSONException e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
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
                if (error.networkResponse.data != null) {
                    try {
//                        if (error.networkResponse.statusCode == 401) {
//                            Toast.makeText(context.getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(context.getApplicationContext(), MainActivity.class);
//                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            preferenceManager.clear();
//                            context.startActivity(i);
//                        }
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
                params.put("Authorization", preferenceManager.getString(Program.TOKEN));
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
                if (error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 401) {

                        new AsyncTasks() {
                            @Override
                            public void onPreExecute() {
                            }

                            @Override
                            public void doInBackground() {
                                Intent i = new Intent(getActivity(), LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                preferenceManager.clear();
                                startActivity(i);
                            }

                            @Override
                            public void onPostExecute() {
                                Toast.makeText(getActivity(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onClick(Job job) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", true);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, int position, ListViewItemJobBinding listViewItemJobBinding) {
        try {
            saveJob(job.getId(), position, listViewItemJobBinding);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
