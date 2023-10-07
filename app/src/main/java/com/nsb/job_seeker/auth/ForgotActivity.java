package com.nsb.job_seeker.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.databinding.ActivityForgotBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ForgotActivity extends AppCompatActivity {
    private ActivityForgotBinding binding;
    private String base_url = Program.url_dev+"/auth";
    private String sharedPreferencesName = "JobSharedPreference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityForgotBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setEvent();
    }

    private void setEvent() {
        binding.btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(!isEmpty()){
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

    private boolean isEmpty(){
        if(binding.edtEmailForgot.getText().toString().isEmpty() || binding.edtEmailForgot.getText().toString().trim().equals(""))
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

                    SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (isSuccess) {
                        editor.putString("email", binding.edtEmailForgot.getText().toString());
                        Log.d("ABC", "send mail doInBg : "+ isSuccess);
                        Intent i = new Intent(ForgotActivity.this, ConfirmOTP.class);
                        startActivity(i);
                    }
                    editor.commit();
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
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if(error.networkResponse.data!=null) {
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }
}