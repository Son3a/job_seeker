package com.nsb.job_seeker.activity;

import com.nsb.job_seeker.activity.BaseActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityChangePasswordBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Activity_ChangePassword extends BaseActivity {
    private ActivityChangePasswordBinding binding;
    private String base_url = Constant.url_dev + "/auth";

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();
        setEvent();
    }

    private void setControl() {
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setEvent() {
        binding.icBack.setOnClickListener(v -> onBackPressed());
        binding.btnSave.setOnClickListener(v -> {
            if (isEmpty(binding.textCurrentPW)) {
                binding.layoutErrorCurrentPW.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorCurrentPW.setVisibility(View.GONE);
            }
            if (isEmpty(binding.textNewPW)) {
                binding.layoutErrorNewPW.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorNewPW.setVisibility(View.GONE);

            }
            if (isEmpty(binding.textConfirmPW)) {
                binding.textErrorConfirmPW.setText("Vui lòng nhập lại mật khẩu mới");
                binding.layoutErrorConfirmPW.setVisibility(View.VISIBLE);
            } else {
                binding.layoutErrorConfirmPW.setVisibility(View.GONE);

            }
            if(!binding.textConfirmPW.getText().toString().trim().equals(binding.textNewPW.getText().toString().trim())){
                binding.textErrorConfirmPW.setText("Mật khẩu xác nhận không khớp!");
                binding.layoutErrorConfirmPW.setVisibility(View.VISIBLE);
            }
            if (!isEmpty(binding.textConfirmPW) && !isEmpty(binding.textNewPW) && !isEmpty(binding.textCurrentPW)) {
                binding.textErrorConfirmPW.setText("Vui lòng nhập lại mật khẩu mới");
                binding.layoutErrorCurrentPW.setVisibility(View.GONE);
                binding.layoutErrorNewPW.setVisibility(View.GONE);
                binding.layoutErrorConfirmPW.setVisibility(View.GONE);
                try {
                    changPasswordService(binding.textCurrentPW.getText().toString(), binding.textNewPW.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            if (binding.tiePassword.getText().toString().equals("") || binding.tieConfirmPassword.getText().toString().equals("")
//                     || binding.tiePasswordCurrent.getText().toString().equals("")) {
//                dialogNotification.openDialogNotification("Không được bỏ trống ô nào !", Activity_ChangePassword.this);
//            } else if (!tiePassword.getText().toString().equals(tieConfirmPassword.getText().toString())) {
//                dialogNotification.openDialogNotification("Xác nhận mật khẩu không khớp, vui lòng nhập lại !", Activity_ChangePassword.this);
//            } else {
//                try {
//                    loadingDialog.startLoadingDialog();
//                    changPasswordService(tiePasswordCurrent.getText().toString(), tieConfirmPassword.getText().toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
        });
    }

    private boolean isEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty() || editText.getText().toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    private void changPasswordService(String password, String newPassword) throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(Activity_ChangePassword.this);
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);

                if (error.networkResponse.data != null) {
                    try {
                        if (error instanceof com.android.volley.NoConnectionError) {

                        } else if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(Activity_ChangePassword.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        } else {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.d("ABC", body);
                            JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                            String message = convertedObject.get("message").toString();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getSharedPreferences(Constant.sharedPreferencesName, MODE_PRIVATE);
                String ACCESSTOKEN = sharedPreferences.getString("accessToken", "");
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
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
                if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                    Intent i = new Intent(Activity_ChangePassword.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void changePasswordFirebase(String newPassword, String message) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constant.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constant.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constant.KEY_PASSWORD, newPassword);
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    CustomToast.makeText(Activity_ChangePassword.this,"Đổi mật khẩu thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    finish();
                    Log.d("ABC", "Firebase: Successfully!");
                })
                .addOnFailureListener(e -> Log.d("Error", e.getMessage()));
    }

}