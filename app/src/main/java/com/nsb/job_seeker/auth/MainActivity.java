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
import com.nsb.job_seeker.common.AsyncTasks;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.employer.EmployerMainActivity;
import com.nsb.job_seeker.seeker.SeekerMainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtWarning, txtRedirectRegister, txtForgotPassword;
    private CheckBox cbxRemeberPassword;

    private RequestQueue mRequestQueue;
    private String base_url = Program.url_dev + "/auth";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private PreferenceManager preferenceManager;
    private String tokenDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.getBoolean(Program.KEY_IS_SIGNED_IN)) {
            Program.idSavedJobs = preferenceManager.getArray(Program.LIST_SAVED_JOB);
//            preferenceManager.putString(Program.TOKEN, "Bearer asd");
            redirectAfterLogin();
        }


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

                        tokenDevice = token;
                        // Log and toast
                        Log.d("ABC", token);
                    }
                });

        this.loadingDialog = new LoadingDialog(MainActivity.this);
        setControl();
        setEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName, MODE_PRIVATE);
        String isRemember = sharedPreferences.getString("isRememberPassword", "false");

        if (isRemember.equals("true")) {
            String email = sharedPreferences.getString("email", "");
            String pw = sharedPreferences.getString("password", "");
            edtEmail.setText(email);
            edtPassword.setText(pw);
            cbxRemeberPassword.setChecked(true);
        } else {
            cbxRemeberPassword.setChecked(false);
        }
    }

    private void setControl() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtWarning = findViewById(R.id.txtWarning);
        txtRedirectRegister = findViewById(R.id.txtRedirectRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        cbxRemeberPassword = findViewById(R.id.cbxRemember);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();
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
                    preferenceManager.putString("accessToken", accessToken);
                    preferenceManager.putString("refreshToken", refreshToken);

                    signIn();   //login firebase


                    SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("accessToken", accessToken);
                    editor.putString("refreshToken", refreshToken);
                    if (cbxRemeberPassword.isChecked()) {
                        editor.putString("isRememberPassword", "true");
                        editor.putString("email", edtEmail.getText().toString());
                        editor.putString("password", edtPassword.getText().toString());
                    } else {
                        editor.putString("isRememberPassword", "false");
                        editor.putString("email", "");
                        editor.putString("password", "");
                    }
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
                    String avatar = response.getString("avatar");

                    preferenceManager.putString(Program.USER_ID, response.getString("_id"));
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
//                            Log.d("ABC", listSavedJob.get(i));
                        }
//                        Log.d("Jobsaved", preferenceManager.getArray(Program.LIST_SAVED_JOB).toString());
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


                    preferenceManager.putBoolean(Program.KEY_IS_SIGNED_IN, true);
                    redirectAfterLogin();

                    Program.avatar = avatar;
                  
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Job", "LOI 1 : " + e.toString());
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
                        if (error.networkResponse.statusCode == 401) {
                            Toast.makeText(getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(),MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
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
                if(error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 401) {

                        new AsyncTasks() {
                            @Override
                            public void onPreExecute() {
                            }

                            @Override
                            public void doInBackground() {
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                preferenceManager.clear();
                                startActivity(i);
                            }

                            @Override
                            public void onPostExecute() {
                                Toast.makeText(getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void redirectAfterLogin() {
        if (preferenceManager.getString(Program.ROLE).trim().equals("user")) {
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
    }


    private void signIn() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Program.KEY_COLLECTION_USERS)
                .whereEqualTo(Program.KEY_EMAIL, edtEmail.getText().toString())
                .whereEqualTo(Program.KEY_PASSWORD, edtPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null &&
                                    task.getResult().getDocuments().size() > 0) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                preferenceManager.putString(Program.KE_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Program.KEY_NAME, documentSnapshot.getString(Program.KEY_NAME));
                                preferenceManager.putString(Program.KEY_IMAGE, documentSnapshot.getString(Program.KEY_IMAGE));
                            }
                        }
                ).addOnFailureListener(e -> {
                    Toast.makeText(this, "Cant not sign in", Toast.LENGTH_SHORT).show();
                });
    }

}