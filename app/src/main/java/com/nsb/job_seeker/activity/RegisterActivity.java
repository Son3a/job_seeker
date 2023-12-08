package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityRegisterBinding;
import com.nsb.job_seeker.listener.EventKeyboard;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity extends BaseActivity {
    private ActivityRegisterBinding binding;
    private String base_url = Constant.url_dev + "/auth";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        setContentView(binding.getRoot());
        this.loadingDialog = new LoadingDialog(RegisterActivity.this);

        setControl();
        setEvent();
    }

    private void setControl() {
        loadingDialog = new LoadingDialog(RegisterActivity.this);
        preferenceManager = new PreferenceManager(this);
    }

    private void setEvent() {
        clickRegister();
        gotoLogin();
        gotoAppWithoutLogin();
        setStateConfirmPassword();
        eventFocus();
        hideKeyBoard();
    }

    private void hideKeyBoard() {
        binding.layoutMain.setOnClickListener(v -> {
            Constant.hideKeyboardFrom(RegisterActivity.this, v);
        });
    }

    private void eventFocus() {
        Constant.eventKeyBoard(binding.getRoot(), new EventKeyboard() {
            @Override
            public void hideKeyboard() {
                binding.textName.setCursorVisible(false);
                binding.textEmail.setCursorVisible(false);
                binding.textPassword.setCursorVisible(false);
                binding.textConfirmPassword.setCursorVisible(false);
                Log.e("MyActivity", "keyboard closed");

                // Toast.makeText(RegisterActivity.this, "hide keyboard", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showKeyboard() {
                binding.textName.setCursorVisible(true);
                binding.textEmail.setCursorVisible(true);
                binding.textPassword.setCursorVisible(true);
                binding.textConfirmPassword.setCursorVisible(true);
                Log.e("MyActivity", "keyboard opened");
                //Toast.makeText(RegisterActivity.this, "show keyboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStateConfirmPassword() {
        binding.textConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isEmpty(binding.textPassword)) {
                    if (binding.textConfirmPassword.getText().toString().equals(binding.textPassword.getText().toString())) {
                        binding.layoutErrorConfirmPassword.setVisibility(View.GONE);
                        binding.layoutConfirmPasswordCorrect.setVisibility(View.VISIBLE);
                    } else {
                        binding.textErrorConfirmPW.setText("Mật khẩu không khớp");
                        binding.layoutErrorConfirmPassword.setVisibility(View.VISIBLE);
                        binding.layoutConfirmPasswordCorrect.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void gotoAppWithoutLogin() {
        binding.textVisitPage.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeekerMainActivity.class);
            preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void clickRegister() {
        binding.btnRegister.setOnClickListener(v -> {

            //validate email
            if (isEmpty(binding.textEmail)) {
                binding.layoutErrorEmail.setVisibility(View.VISIBLE);
                binding.textEmail.setBackgroundResource(R.drawable.background_edittext_error);
            } else {
                binding.layoutErrorEmail.setVisibility(View.GONE);
                binding.textEmail.setBackgroundResource(R.drawable.background_edittext_register);
            }

            //validate password
            if (isEmpty(binding.textPassword)) {
                binding.layoutErrorPassword.setVisibility(View.VISIBLE);
                binding.textPassword.setBackgroundResource(R.drawable.background_edittext_error);
            } else {
                binding.layoutErrorPassword.setVisibility(View.GONE);
                binding.textPassword.setBackgroundResource(R.drawable.background_edittext_register);
            }

            //validate name
            if (isEmpty(binding.textName)) {
                binding.layoutErrorName.setVisibility(View.VISIBLE);
                binding.textName.setBackgroundResource(R.drawable.background_edittext_error);
            } else {
                binding.layoutErrorName.setVisibility(View.GONE);
                binding.textName.setBackgroundResource(R.drawable.background_edittext_register);
            }

            //validate confirm password
            if (isEmpty(binding.textConfirmPassword)) {
                binding.layoutErrorConfirmPassword.setVisibility(View.VISIBLE);
                binding.textErrorConfirmPW.setText("Cho chúng tôi biết xác nhận mật khẩu của bạn");
                binding.textConfirmPassword.setBackgroundResource(R.drawable.background_edittext_error);
            } else {
                binding.layoutErrorConfirmPassword.setVisibility(View.GONE);
                binding.textConfirmPassword.setBackgroundResource(R.drawable.background_edittext_register);
            }

            if (!binding.textConfirmPassword.getText().toString().trim().equals(binding.textPassword.getText().toString().trim())) {
                binding.layoutConfirmPasswordCorrect.setVisibility(View.GONE);
                binding.textErrorConfirmPW.setText("Mật khẩu không khớp");
                binding.layoutErrorConfirmPassword.setVisibility(View.VISIBLE);
            }


            if (!isEmpty(binding.textName) && !isEmpty(binding.textPassword) && !isEmpty(binding.textEmail)
                    && !isEmpty(binding.textConfirmPassword) && binding.cbxAcceptApp.isChecked() &&
                    binding.textConfirmPassword.getText().toString().trim().equals(binding.textPassword.getText().toString().trim())) {
                binding.layoutErrorConfirmPassword.setVisibility(View.GONE);
                binding.textConfirmPassword.setBackgroundResource(R.drawable.background_edittext_register);
                binding.layoutErrorName.setVisibility(View.GONE);
                binding.textName.setBackgroundResource(R.drawable.background_edittext_register);
                binding.layoutErrorPassword.setVisibility(View.GONE);
                binding.textPassword.setBackgroundResource(R.drawable.background_edittext_register);
                binding.layoutErrorEmail.setVisibility(View.GONE);
                binding.textEmail.setBackgroundResource(R.drawable.background_edittext_register);

                try {
                    registerApi();
                } catch (JSONException e) {
                    e.printStackTrace();
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

    private void registerApi() throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
        loadingDialog.showDialog();

        String role = "admin";
        if (binding.switchButton.getIsChecked()) {
            role = "user";
        }

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("password", binding.textPassword.getText().toString());
        jsonReq.put("name", binding.textName.getText().toString());
        jsonReq.put("email", binding.textEmail.getText().toString());
        jsonReq.put("phone", "");
        jsonReq.put("role", role);

        String finalRole = role;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constant.url_dev + "/auth/register", jsonReq, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("data");
                    preferenceManager.putString(Constant.TOKEN, "Bearer " + jsonObject.getString("refreshToken"));
                    preferenceManager.putString(Constant.NAME, jsonObject.getString("name"));
                    preferenceManager.putString(Constant.MAIL, jsonObject.getString("email"));
                    if (!jsonObject.getString("avatar").isEmpty()) {
                        preferenceManager.putString(Constant.AVATAR, jsonObject.getString("avatar"));
                        preferenceManager.putString(Constant.PHONE, jsonObject.getString("phone"));
                    }
                    preferenceManager.putString(Constant.ROLE, jsonObject.getString("role"));

                    Constant.idAppliedJob = new ArrayList<>();
                    Constant.idAppliedJob = new ArrayList<>();
                    Log.d("TokenWeb", preferenceManager.getString(Constant.TOKEN));
                    signUp(jsonObject.getString("_id"), finalRole); // sign up on firebase
                } catch (JSONException e) {
                    loadingDialog.hideDialog();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
//                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                loadingDialog.hideDialog();
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();
                        CustomToast.makeText(RegisterActivity.this, message.substring(1, message.length() - 1), CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void signUp(String id, String role) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constant.KEY_USER_ID, id);
        user.put(Constant.KEY_NAME, binding.textName.getText().toString());
        user.put(Constant.KEY_EMAIL, binding.textEmail.getText().toString());
        user.put(Constant.KEY_PASSWORD, binding.textPassword.getText().toString());
        user.put(Constant.KEY_IMAGE, "");
        user.put(Constant.KEY_AVAILABILITY, 0);
        user.put(Constant.KEY_FCM_TOKEN, "");
        database.collection(Constant.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loadingDialog.hideDialog();
                    preferenceManager.putString(Constant.KEY_USER_ID, documentReference.getId());
                    documentReference.get().addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        preferenceManager.putString(Constant.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constant.KEY_NAME, documentSnapshot.getString(Constant.KEY_NAME));
                        Log.d("NameFB", documentSnapshot.getString(Constant.KEY_NAME));
                        preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                        redirectToApp(role);
                    });
                });
    }

    private void gotoLogin() {
        binding.textLogin.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void redirectToApp(String role) {
        Log.d("Loop", "Loop");
        if (role.trim().equals("user")) {
            CustomToast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
            Intent intent = new Intent(this, SeekerMainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, NameCompanyActivity.class);
            startActivity(intent);
        }
    }
}