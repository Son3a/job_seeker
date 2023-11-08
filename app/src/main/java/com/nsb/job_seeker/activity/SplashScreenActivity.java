package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivitySplashScreenBinding;
import com.nsb.job_seeker.activity.admin.EmployerMainActivity;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashScreenActivity extends BaseActivity {
    private ActivitySplashScreenBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(SplashScreenActivity.this);

        if(preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            getAppliedJob();
        } else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                }
            }, 2000);
        }
    }

    private void getSavedJob() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Constant.url_dev + "/auth/info-user";

        String token = preferenceManager.getString(Constant.TOKEN);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Constant.idSavedJobs = new ArrayList<>();
                    JSONArray listJobFavorite = response.getJSONArray("jobFavourite");
                    for (int i = 0; i < listJobFavorite.length(); i++) {
                        if (!listJobFavorite.getJSONObject(i).isNull("jobId")) {
                            JSONObject jobObject = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                            if (jobObject.getString("status").equals("true")) {
                                Constant.idSavedJobs.add(jobObject.getString("_id"));
                            }
                        }
                    }
                    redirectAfterLogin(preferenceManager.getString(Constant.ROLE));
                } catch (JSONException e) {
                    Log.d("Error", e.toString());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "Error response " + error.toString());
                if (error instanceof com.android.volley.NoConnectionError) {
                    CustomToast.makeText(SplashScreenActivity.this, "Hệ thống đang có lỗi, quý khách vui lòng quay lại sau!",
                            CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                } else if (error.networkResponse.data != null && (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 400)) {
                    Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", token);
                return params;
            }
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
        requestQueue.add(jsonObjectRequest);
    }

    private void getAppliedJob() {
        String url = Constant.url_dev + "/application/get-by-userid";
        String token = preferenceManager.getString(Constant.TOKEN);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest data = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Constant.idAppliedJob = new ArrayList<>();
                            JSONArray jobsList = response.getJSONObject("data").getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i).getJSONObject("idJob");
                                Constant.idAppliedJob.add(job.getString("_id"));
                            }
                            getSavedJob();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof com.android.volley.NoConnectionError) {

                        } else if (error.networkResponse.statusCode == 401 && error.networkResponse.data != null) {
                            Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
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
                throw new VolleyError(error.getMessage());
            }
        });
        queue.add(data);
    }

    private void redirectAfterLogin(String role) {
        if (role.trim().equals("user")) {
            Log.d("ABC", "user");
            Intent intent = new Intent(this, SeekerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.d("ABC", "admin");
            Intent intent = new Intent(this, EmployerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}