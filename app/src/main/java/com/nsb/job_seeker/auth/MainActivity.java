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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtWarning, txtRedirectRegister, txtForgotPassword;
    private Toolbar toolbar;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String base_url = Program.url_dev + "/auth";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loadingDialog = new LoadingDialog(MainActivity.this);
        Log.d("ABC", base_url);
        setControl();
        setEvent();
    }

    private void setControl() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtWarning = findViewById(R.id.txtWarning);
        txtRedirectRegister = findViewById(R.id.txtRedirectRegister);
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

                    String accessToken = convertedObject.get("accessToken").toString();
                    Program.token = "Bearer " + accessToken.replace("\"", "");
                    String refreshToken = convertedObject.get("refreshToken").toString();
                    Log.d("ABC", accessToken);

                    SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName, MODE_PRIVATE);
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
                if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), MainActivity.this);
                    } catch (UnsupportedEncodingException e) {
                        Log.d("ABC", e.toString());
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
                    String name = response.getString("name");
                    String email = response.getString("email");
                    String phone = response.getString("phone");
//                    String avatar = response.getString("avatar");

                    Program.idUser = response.getString("_id");
                    if (role.equals("admin")) {
                        Program.idCompany = response.getJSONObject("company").getString("_id");
                    } else {
                        Program.idListJobSaved = new ArrayList<>();
                        JSONArray listJobFavorite = response.getJSONArray("jobFavourite");
                        for (int i = 0; i < listJobFavorite.length(); i++) {
                            JSONObject job = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                            Program.idListJobSaved.add(job.getString("_id"));
                            Log.d("ABC",Program.idListJobSaved.get(i));
                        }
                    }
                    Program.role = role;

                    SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.putString("email", email);
                    editor.putString("phone", phone);

                    editor.commit();
                    loadingDialog.dismissDialog();

                    if (role.trim().equals("user")) {
                        Log.d("ABC", "user");
                        Intent intent = new Intent(MainActivity.this, SeekerMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Log.d("ABC", "admin");
                        Intent intent = new Intent(MainActivity.this, EmployerMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("ABC", "LOI 1 : " + e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                    } catch (UnsupportedEncodingException e) {
                        Log.d("ABC", "LOI 2 : " + e.toString());
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
                params.put("Authorization", "Bearer " + ACCESSTOKEN);
                return params;
            }
        };
        mRequestQueue.add(jsonObjectRequest);
    }

}