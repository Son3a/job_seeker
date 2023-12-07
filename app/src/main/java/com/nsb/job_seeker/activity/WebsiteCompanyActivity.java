package com.nsb.job_seeker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.nsb.job_seeker.activity.admin.EmployerMainActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityWebsiteCompanyBinding;
import com.nsb.job_seeker.model.Company;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class WebsiteCompanyActivity extends AppCompatActivity {
    private ActivityWebsiteCompanyBinding binding;
    private LoadingDialog loadingDialog;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityWebsiteCompanyBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        loadingDialog = new LoadingDialog(WebsiteCompanyActivity.this);
        preferenceManager = new PreferenceManager(getApplicationContext());

        clickNext();
    }

    private void clickNext() {
        binding.buttonNext.setOnClickListener(v -> {
            if (binding.textWebsiteCompany.getText().toString().isEmpty()) {
                binding.layoutErrorWebsite.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorWebsite.setVisibility(View.GONE);
                createCompany();
            }
        });
    }

    private void createCompany() {
        Company company = (Company) getIntent().getSerializableExtra(Constant.COMPANY_MODEL);
        RequestQueue mRequestQueue = Volley.newRequestQueue(WebsiteCompanyActivity.this);
        loadingDialog.showDialog();
        //post data
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", company.getName());
            jsonObject.put("address", company.getAddress());
            jsonObject.put("about", company.getAbout());
            jsonObject.put("totalEmployee", company.getTotalEmployee());
            jsonObject.put("link", binding.textWebsiteCompany.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constant.url_dev + "/company/create", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    //changePasswordFirebase(newPassword, message);
                    loadingDialog.hideDialog();
                    CustomToast.makeText(WebsiteCompanyActivity.this, "Đăng ký thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    finish();
                } catch (JSONException e) {
                    Log.d("ABC", e.toString());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.hideDialog();
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);

                if (error.networkResponse.data != null) {
                    try {
                        if (error instanceof com.android.volley.NoConnectionError) {

                        } else if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(WebsiteCompanyActivity.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        } else {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.d("ABC", body);
                            JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                            String message = convertedObject.get("message").toString();
                            CustomToast.makeText(WebsiteCompanyActivity.this, message, CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getSharedPreferences(Constant.sharedPreferencesName, MODE_PRIVATE);
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
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
        mRequestQueue.add(jsonObjectRequest);
    }
}