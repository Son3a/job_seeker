package com.nsb.job_seeker.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.nsb.job_seeker.employer.EmployerMainActivity;
import com.nsb.job_seeker.seeder.SeekerMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtWarning, txtRedirectRegister, txtForgotPassword;
    private Toolbar toolbar;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String base_url = Program.url_dev+"/auth";
    private String sharedPreferencesName = "JobSharedPreference";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loadingDialog = new LoadingDialog(MainActivity.this);
        setControl();
        setEvent();
    }

    private void setControl() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtWarning = findViewById(R.id.txtWarning);
        txtRedirectRegister= findViewById(R.id.txtRedirectRegister);
        toolbar = findViewById(R.id.myToolbar);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
//        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void setEvent() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtEmail.getText().toString().equals("") || edtPassword.getText().toString().equals("")) {
                    txtWarning.setText("Không được bỏ trống email và password !");
                    txtWarning.setVisibility(View.VISIBLE);
                } else {
                    txtWarning.setVisibility(View.GONE);
                    loadingDialog.startLoadingDialog();
                    try {
                        sendAndRequestResponse(edtEmail.getText().toString(), edtPassword.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        txtRedirectRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ForgotActivity.class);
                startActivity(i);
            }
        });
    }

    private void sendAndRequestResponse(String email, String password) throws JSONException {
        //RequestQueue initialized
////            1.Tạo request
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", email);
        jsonObject.put("password", password);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, base_url + "/login", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JsonObject convertedObject = new Gson().fromJson(response.getString("data"), JsonObject.class);
                    Program.token = convertedObject.get("accessToken").toString();
                    String accessToken = convertedObject.get("accessToken").toString();
                    String refreshToken = convertedObject.get("refreshToken").toString();
                    Log.d("ABC", accessToken);

                    SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("accessToken", accessToken);
                    editor.putString("refreshToken", refreshToken);
                    editor.commit();
                    getInfoUser(accessToken);
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring( 1, message.length() - 1 ), MainActivity.this);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                loadingDialog.dismissDialog();

            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void getInfoUser(String accessToken) {
        StringBuilder sb = new StringBuilder(accessToken);
        sb.deleteCharAt(0);
        sb.deleteCharAt(accessToken.length() - 2);
        String ACCESSTOKEN = sb.toString();
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, base_url + "/info-user", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String role = response.getString("role");

                    Log.d("ABC", role);
                    loadingDialog.dismissDialog();

                    if(role.trim().equals("user")){
                        startActivity(new Intent(MainActivity.this, SeekerMainActivity.class));
                    }
                    else{
                        startActivity(new Intent(MainActivity.this, EmployerMainActivity.class));
                    }
                } catch (JSONException e) {
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
                        Log.d("ABC", body);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    loadingDialog.dismissDialog();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer "+ACCESSTOKEN);
                return params;
            }
        };
        mRequestQueue.add(jsonObjectRequest);
    }

}