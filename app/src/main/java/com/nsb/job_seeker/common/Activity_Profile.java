package com.nsb.job_seeker.common;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
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
import com.nsb.job_seeker.auth.Activity_ChangePassword;
import com.nsb.job_seeker.auth.DialogNotification;
import com.nsb.job_seeker.auth.LoadingDialog;
import com.nsb.job_seeker.auth.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Activity_Profile extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String base_url = Program.url_dev+"/auth";
    private String sharedPreferencesName = "JobSharedPreference";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;

    private EditText edtName, edtEmail, edtPhone;
    private Button btnChangeProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        this.loadingDialog = new LoadingDialog(Activity_Profile.this);
        setControl();
        initData();
        setEvent();
    }

    private void setEvent() {
        btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtName.getText().toString().equals("") || edtEmail.getText().toString().equals("") || edtPhone.getText().toString().equals("")) {
                    dialogNotification.openDialogNotification("Không được bỏ trống bất kỳ mục nào !", Activity_Profile.this);
                } else {
                    loadingDialog.startLoadingDialog();
                    try {
                        changProfileService(edtName.getText().toString(), edtEmail.getText().toString(), edtPhone.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void setControl() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnChangeProfile = findViewById(R.id.btnChangeProfile);
    }

    private void initData() {
        SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName,  MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");
        Log.d("ABC", name + email + phone);
        edtName.setText(name);
        edtPhone.setText(phone);
        edtEmail.setText(email);

    }

    private void changProfileService(String name, String email, String phone) throws JSONException {
        mRequestQueue = Volley.newRequestQueue(Activity_Profile.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("email", email);
        jsonObject.put("phone", phone);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, base_url + "/edit-profile", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    Log.d("ABC", message);

                    dialogNotification.openDialogNotification(message, Activity_Profile.this);
                } catch (JSONException e) {
                    Log.d("ABC",e.toString());
                    e.printStackTrace();
                }
                loadingDialog.dismissDialog();
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
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring( 1, message.length() - 1 ), Activity_Profile.this);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                loadingDialog.dismissDialog();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName,  MODE_PRIVATE);
                String ACCESSTOKEN = sharedPreferences.getString("accessToken", "");
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer "+ACCESSTOKEN.substring(1, ACCESSTOKEN.length()-1));
                return params;
            }
        };;
        mRequestQueue.add(jsonObjectRequest);
    }

}