package com.nsb.job_seeker.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Activity_ChangePassword extends AppCompatActivity {
    private Button btnChangePassword;
    private TextInputEditText tieConfirmPassword, tiePassword, tiePasswordCurrent;
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private String base_url = Program.url_dev + "/auth";
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private ImageView imgBack;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        this.loadingDialog = new LoadingDialog(Activity_ChangePassword.this);
        setControl();
        setEvent();
    }

    private void setControl() {
        btnChangePassword = findViewById(R.id.btnChangePassword);
        tiePassword = findViewById(R.id.tiePassword);
        tieConfirmPassword = findViewById(R.id.tieConfirmPassword);
        tiePasswordCurrent = findViewById(R.id.tiePasswordCurrent);
        imgBack = findViewById(R.id.backArrow);
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setEvent() {
        imgBack.setOnClickListener(v -> onBackPressed());
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tiePassword.getText().toString().equals("") || tieConfirmPassword.getText().toString().equals("") || tiePasswordCurrent.getText().toString().equals("")) {
                    dialogNotification.openDialogNotification("Không được bỏ trống ô nào !", Activity_ChangePassword.this);
                } else if (!tiePassword.getText().toString().equals(tieConfirmPassword.getText().toString())) {
                    dialogNotification.openDialogNotification("Xác nhận mật khẩu không khớp, vui lòng nhập lại !", Activity_ChangePassword.this);
                } else {
                    try {
                        loadingDialog.startLoadingDialog();
                        changPasswordService(tiePasswordCurrent.getText().toString(), tieConfirmPassword.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void changPasswordService(String password, String newPassword) throws JSONException {
        mRequestQueue = Volley.newRequestQueue(Activity_ChangePassword.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("password", password);
        jsonObject.put("newPassword", newPassword);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, base_url + "/change-password", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    Log.d("ABC", message);
                    changePasswordFirebase(newPassword, message);
                } catch (JSONException e) {
                    Log.d("ABC", e.toString());
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

                if (error.networkResponse.data != null) {
                    try {
                        if (error.networkResponse.statusCode == 401) {
                            Toast.makeText(getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), Activity_ChangePassword.this);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                loadingDialog.dismissDialog();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getSharedPreferences(Program.sharedPreferencesName, MODE_PRIVATE);
                String ACCESSTOKEN = sharedPreferences.getString("accessToken", "");
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Program.TOKEN));
                return params;
            }
        };
        ;
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

            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void changePasswordFirebase(String newPassword, String message) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Program.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Program.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Program.KEY_PASSWORD, newPassword);
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    Log.d("ABC", "Firebase: Successfully!");
                })
                .addOnFailureListener(e -> Log.d("Error", e.getMessage()));
    }

}