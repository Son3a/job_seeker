package com.nsb.job_seeker.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityLoginBinding;
import com.nsb.job_seeker.employer.EmployerMainActivity;
import com.nsb.job_seeker.seeker.SeekerMainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private String base_url = Program.url_dev + "/auth";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private PreferenceManager preferenceManager;
    private String tokenDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.getBoolean(Program.KEY_IS_SIGNED_IN)) {

            Program.idSavedJobs = preferenceManager.getArray(Program.LIST_SAVED_JOB);
            redirectAfterLogin(preferenceManager.getString(Program.ROLE));
        }

        getToken();

        this.loadingDialog = new LoadingDialog(LoginActivity.this);

        setEvent();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("ABC", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
//                        preferenceManager.putString(Program.TOKEN, token);
                        tokenDevice = token;
                        // Log and toast
                        Log.d("ABC", token);
                    }
                });
    }

    private void setEvent() {
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.textEmail.getText().toString().equals("") || binding.textPassword.getText().toString().equals("")) {
                    Toast.makeText(LoginActivity.this, "Không được bỏ trống email và password !", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        login(binding.textEmail.getText().toString().trim(), binding.textPassword.getText().toString().trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        binding.textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        binding.textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, ForgotActivity.class);
                startActivity(i);
            }
        });
    }

    private void login(String email, String password) throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(LoginActivity.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", email);
        jsonObject.put("password", password);
        jsonObject.put("tokenDevice", tokenDevice);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, base_url + "/login", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JsonObject convertedObject = new Gson().fromJson(response.getString("data"), JsonObject.class);
                    String accessToken = convertedObject.get("accessToken").toString();
                    String refreshToken = convertedObject.get("refreshToken").toString();
                    preferenceManager.putString(Program.TOKEN, "Bearer " + accessToken.replace("\"", ""));
                    preferenceManager.putString(Program.REFRESH_TOKEN, "Bearer " + refreshToken.replace("\"", ""));
                    Log.d("Process", "Login monggo successfully!");
                    signIn(email, password);   //login firebase

                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                if (error.networkResponse != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), LoginActivity.this);
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

    private void getInfoUser() {
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        Log.d("Process", "get info");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, base_url + "/info-user", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String role = response.getString("role");
                    String name = response.getString("name");
                    String email = response.getString("email");
                    String phone = response.getString("phone");
                    String avatar = response.getString("avatar");

                    Program.idSavedJobs = new ArrayList<>();
                    if (role.equals(Program.ADMIN_ROLE)) {
                        preferenceManager.putString(Program.COMPANY_ID, response.getJSONObject("company").getString("_id"));
                    } else {
                        JSONArray listJobFavorite = response.getJSONArray("jobFavourite");
                        for (int i = 0; i < listJobFavorite.length(); i++) {
                            JSONObject jobObject = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                            if (jobObject.getString("status").equals("true")) {
                                Program.idSavedJobs.add(jobObject.getString("_id"));
                            }
                        }
                    }
                    preferenceManager.putArray(Program.idSavedJobs);
                    preferenceManager.putString(Program.ROLE, role);


                    SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.putString("email", email);
                    editor.putString("phone", phone);

                    editor.commit();
                    loadingDialog.dismissDialog();

                    preferenceManager.putBoolean(Program.KEY_IS_SIGNED_IN, true);//
                    Program.avatar = avatar;
                    Log.d("Process", "get info successfully!");
                    redirectAfterLogin(role);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Job", "LOI 1 : " + e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "err " + error.toString());
                String body = "";
                //get status code here
                if (error.networkResponse != null && error.networkResponse.data != null) {
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
                Log.d("Auth", preferenceManager.getString(Program.TOKEN));
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Program.TOKEN));
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
                Log.d("Error", error.toString());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void redirectAfterLogin(String role) {
        if (role.trim().equals("user")) {
            Log.d("ABC", "user");
            Intent intent = new Intent(LoginActivity.this, SeekerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.d("ABC", "admin");
            Intent intent = new Intent(LoginActivity.this, EmployerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    private void signIn(String email, String password) {
        Log.d("Process", "Login Firebase");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Program.KEY_COLLECTION_USERS)
                .whereEqualTo(Program.KEY_EMAIL, email)
                .whereEqualTo(Program.KEY_PASSWORD, password)
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null &&
                                    task.getResult().getDocuments().size() > 0) {

                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                Log.d("UserId", documentSnapshot.getId());
                                preferenceManager.putString(Program.KEY_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Program.KEY_NAME, documentSnapshot.getString(Program.KEY_NAME));
                                preferenceManager.putString(Program.KEY_IMAGE, documentSnapshot.getString(Program.KEY_IMAGE));

                                Log.d("Process", "Login Firebase successfully");
                                getInfoUser();
                            }
                        }
                ).addOnFailureListener(e -> {
                    Log.d("Error", e.getMessage());
                });
    }

}