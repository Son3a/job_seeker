package com.nsb.job_seeker.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SetNewPassword extends AppCompatActivity {
    private String code;
    private TextInputEditText edtPassword, edtPasswordConfirm;
    private Button btnConfirm;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private TextView txtWarningResetPassword;
    private String base_url = Program.url_dev+"/auth";
    private String sharedPreferencesName = "JobSharedPreference";
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Đặt lại mật khẩu");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DB3AA1F6")));
        setContentView(R.layout.activity_set_new_password);
        this.loadingDialog = new LoadingDialog(SetNewPassword.this);
        Intent i = getIntent();
        this.code = i.getStringExtra("code");
        Log.d("ABC", this.code.toString());
        setControl();
        setEvent();
    }

    private void setControl() {
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
                    loadingDialog.startLoadingDialog();
                    confirmResetPassword();
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
                loadingDialog.dismissDialog();
                try {
                    txtWarningResetPassword.setVisibility(View.GONE);
                    String message = response.getString("message");
                    Log.d("ABC", message);
                } catch (JSONException e) {
                    Toast.makeText(SetNewPassword.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.dismissDialog();
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if(error.networkResponse.data!=null) {
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);

                        txtWarningResetPassword.setVisibility(View.VISIBLE);
                        txtWarningResetPassword.setText(convertedObject.get("message").toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }
}