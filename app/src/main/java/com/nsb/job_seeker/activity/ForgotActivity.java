package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityForgotBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ForgotActivity extends BaseActivity {
    private ActivityForgotBinding binding;
    private String base_url = Constant.url_dev + "/auth";
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityForgotBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);

        setEvent();
    }

    private void setEvent() {
        binding.btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (!isEmpty()) {
                        binding.edtEmailForgot.setBackgroundResource(R.drawable.background_edittext_register);
                        binding.layoutError.setVisibility(View.GONE);
                        sendMailToResetPassword();
                    } else {
                        binding.edtEmailForgot.setBackgroundResource(R.drawable.background_edittext_error);
                        binding.layoutError.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isEmpty() {
        if (binding.edtEmailForgot.getText().toString().isEmpty() || binding.edtEmailForgot.getText().toString().trim().equals(""))
            return true;
        return false;
    }

    private void sendMailToResetPassword() throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(ForgotActivity.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", binding.edtEmailForgot.getText().toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, base_url + "/forgot-password", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Boolean isSuccess = response.getBoolean("isSuccess");

                    if (isSuccess) {
                        Log.d("ABC", "send mail doInBg : " + isSuccess);
                        Intent i = new Intent(ForgotActivity.this, ConfirmOTP.class);
                        startActivity(i);
                    }
                } catch (JSONException e) {
                    Toast.makeText(ForgotActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                try {
                    if (error instanceof com.android.volley.NoConnectionError) {

                    } else if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                        Intent i = new Intent(ForgotActivity.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        preferenceManager.clear();
                        startActivity(i);
                    } else {
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }
}