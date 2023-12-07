package com.nsb.job_seeker.activity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nsb.job_seeker.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.listener.EventKeyboard;
import com.nsb.job_seeker.common.LoadingDialog;
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
    private boolean isKeyboardShowing = false;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

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
        loadingDialog = new LoadingDialog(this);
    }

    private void setEvent() {
        binding.icBack.setOnClickListener(v -> onBackPressed());
        eventKeyBoard();
        clickChangePW();
    }

    private boolean validate() {
        boolean isValid = true;
        if (isEmpty(binding.textCurrentPW)) {
            binding.layoutErrorCurrentPW.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.layoutErrorCurrentPW.setVisibility(View.GONE);
        }
        if (isEmpty(binding.textNewPW)) {
            isValid = false;
            binding.textErrorNewPW.setText("Vui lòng nhập mật khẩu mới!");
            binding.layoutErrorNewPW.setVisibility(View.VISIBLE);
        } else {
            binding.layoutErrorNewPW.setVisibility(View.GONE);
        }

        if (isEmpty(binding.textConfirmPW)) {
            binding.textErrorConfirmPW.setTextColor(ContextCompat.getColor(Activity_ChangePassword.this, R.color.red));
            binding.textErrorConfirmPW.setText("Vui lòng nhập lại mật khẩu mới");
            binding.layoutErrorConfirmPW.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.layoutErrorConfirmPW.setVisibility(View.GONE);
        }

        if (binding.textNewPW.getText().toString().trim().equals(binding.textCurrentPW.getText().toString().trim())) {
            binding.textErrorNewPW.setText("Trùng với mật khẩu hiện tại!");
            binding.layoutErrorNewPW.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if(!binding.textNewPW.getText().toString().trim().equals(binding.textConfirmPW.getText().toString().trim())){
            binding.textErrorConfirmPW.setText("Mật khẩu không khớp!");
            binding.layoutErrorConfirmPW.setVisibility(View.VISIBLE);
            isValid = false;
        }
        return isValid;
    }

    private void clickChangePW() {
        binding.btnSave.setOnClickListener(v -> {
            if (validate()) {
                try {
                    binding.layoutErrorCurrentPW.setVisibility(View.GONE);
                    binding.layoutErrorNewPW.setVisibility(View.GONE);
                    binding.layoutErrorConfirmPW.setVisibility(View.GONE);
                    changPasswordService(binding.textCurrentPW.getText().toString(), binding.textNewPW.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void eventKeyBoard() {
        Constant.eventKeyBoard(binding.getRoot(), new EventKeyboard() {
            @Override
            public void hideKeyboard() {
                if (isKeyboardShowing) {
                    binding.textConfirmPW.clearFocus();
                    binding.textCurrentPW.clearFocus();
                    binding.textNewPW.clearFocus();
                    isKeyboardShowing = false;
                }
            }

            @Override
            public void showKeyboard() {
                if (!isKeyboardShowing) {
                    isKeyboardShowing = true;
                }
            }
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
        loadingDialog.showDialog();
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("password", password);
        jsonObject.put("newPassword", newPassword);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, base_url + "/change-password", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    //changePasswordFirebase(newPassword, message);
                    loadingDialog.hideDialog();
                    CustomToast.makeText(Activity_ChangePassword.this, "Đổi mật khẩu thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    finish();
                } catch (JSONException e) {
                    Log.d("ABC", e.toString());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.hideDialog();
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
                            CustomToast.makeText(Activity_ChangePassword.this, message, CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
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
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
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
                throw new VolleyError(error.getMessage());
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
                    CustomToast.makeText(Activity_ChangePassword.this, "Đổi mật khẩu thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    finish();
                    Log.d("ABC", "Firebase: Successfully!");
                })
                .addOnFailureListener(e -> Log.d("Error", e.getMessage()));
    }
}