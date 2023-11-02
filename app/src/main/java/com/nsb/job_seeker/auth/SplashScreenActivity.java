package com.nsb.job_seeker.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivitySplashScreenBinding;
import com.nsb.job_seeker.employer.EmployerMainActivity;
import com.nsb.job_seeker.seeker.SeekerMainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {
    private ActivitySplashScreenBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(SplashScreenActivity.this);

        new Handler().postDelayed(runnable, 2000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            preferenceManager = new PreferenceManager(SplashScreenActivity.this);
            if (preferenceManager.getBoolean(Program.KEY_IS_SIGNED_IN)) {

                redirectAfterLogin(preferenceManager.getString(Program.ROLE));
            } else {
                finish();
                startActivity(new Intent(SplashScreenActivity.this, PreviousLoginActivity.class));
            }
        }
    };

    private void getInfo() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Program.url_dev + "/auth/info-user";
        JsonObjectRequest data = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Program.idSavedJobs = new ArrayList<>();
                                JSONArray listJobFavorite = response.getJSONArray("jobFavourite");
                                for (int i = 0; i < listJobFavorite.length(); i++) {
                                    if (!listJobFavorite.getJSONObject(i).isNull("jobId")) {
                                        JSONObject jobObject = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                                        if (jobObject.getString("status").equals("true")) {
                                            Program.idSavedJobs.add(jobObject.getString("_id"));
                                        }
                                    }
                                }

                            redirectAfterLogin(response.getString("role"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", error.getMessage());
                        if (error.networkResponse != null) {
                            //                                body = new String(error.networkResponse.data, "UTF-8");
//                                Log.d("ABC", body);
//                                JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
//                                String message = convertedObject.get("message").toString();

//                                Toast.makeText(LoginActivity.this, message.substring(1, message.length() - 1), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Program.TOKEN));
                return params;
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