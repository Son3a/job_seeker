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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ForgotActivity extends AppCompatActivity {
    private Button btnSendCode;
    private EditText edtEmailForgot;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String base_url = Program.url_dev+"/auth";
    private String sharedPreferencesName = "JobSharedPreference";
    private TextView txtWarningForgot;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        this.loadingDialog = new LoadingDialog(ForgotActivity.this);
        setControl();
        setEvent();
    }

    private void setControl() {
        btnSendCode = findViewById(R.id.btnSendCode);
        edtEmailForgot = findViewById(R.id.edtEmailForgot);
        txtWarningForgot = findViewById(R.id.txtWarningForgot);
    }

    private void setEvent() {

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    loadingDialog.startLoadingDialog();
                    sendMailToResetPassword();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendMailToResetPassword() throws JSONException {
        mRequestQueue = Volley.newRequestQueue(ForgotActivity.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", edtEmailForgot.getText().toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, base_url + "/forgot-password", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingDialog.dismissDialog();
                try {
                    Boolean isSuccess = response.getBoolean("isSuccess");

                    SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (isSuccess) {
                        editor.putString("email", edtEmailForgot.getText().toString());
                        Log.d("ABC", "send mail doInBg : "+ isSuccess);
                        txtWarningForgot.setVisibility(View.GONE);
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
                loadingDialog.dismissDialog();
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if(error.networkResponse.data!=null) {
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);

                        txtWarningForgot.setVisibility(View.VISIBLE);
                        txtWarningForgot.setText(convertedObject.get("message").toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }
}