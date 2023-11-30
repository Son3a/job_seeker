package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nsb.job_seeker.activity.BaseActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SetNewPassword extends BaseActivity {
    private String code;
    private TextInputEditText edtPassword, edtPasswordConfirm;
    private Button btnConfirm;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private TextView txtWarningResetPassword;
    private String base_url = Constant.url_dev + "/auth";
    private String sharedPreferencesName = "JobSharedPreference";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_new_password);
        Intent i = getIntent();
        this.code = i.getStringExtra("code");
        Log.d("ABC", this.code.toString());
        setControl();
        setEvent();
    }

    private void setControl() {
        loadingDialog = new LoadingDialog(getApplicationContext());
        edtPassword = findViewById(R.id.tiePassword);
        edtPasswordConfirm = findViewById(R.id.tieConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirmResetPassword);
        txtWarningResetPassword = findViewById(R.id.txtWarningResetPassword);
    }

    private void setEvent() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (edtPassword.getText().toString().equals(edtPasswordConfirm.getText().toString())) {
                        confirmResetPassword();
                    } else {
                        dialogNotification.openDialogNotification("Mật khẩu xác nhận không khớp !", SetNewPassword.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void confirmResetPassword() throws JSONException {
        mRequestQueue = Volley.newRequestQueue(SetNewPassword.this);

        SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "abc@gmail.com");
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("confirmPasswordCode", this.code);
        jsonObject.put("newPassword", edtPassword.getText().toString());
        jsonObject.put("email", email);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, base_url + "/confirm-password", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    txtWarningResetPassword.setVisibility(View.GONE);
                    String message = response.getString("message");
                    dialogNotification.openDialogNotification(message, SetNewPassword.this);
                    Log.d("ABC", message);
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } catch (JSONException e) {
                    Toast.makeText(SetNewPassword.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();
                        txtWarningResetPassword.setVisibility(View.VISIBLE);
                        dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), SetNewPassword.this);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }
}