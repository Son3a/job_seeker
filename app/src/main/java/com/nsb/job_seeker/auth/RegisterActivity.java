package com.nsb.job_seeker.auth;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityRegisterBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private String base_url = Program.url_dev + "/auth";
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
        preferenceManager = new PreferenceManager(this);
    }

    private void setEvent() {
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.textEmail.getText().toString().equals("") || binding.textEmail.getText().toString().equals("") || binding.textName.getText().toString().equals("") || binding.textPassword.getText().toString().equals("") || binding.textConfirmPassword.getText().toString().equals("")) {
                    dialogNotification.openDialogNotification("Không được để trống bất kỳ ô nào !", RegisterActivity.this);

                } else if (!binding.textPassword.getText().toString().equals(binding.textConfirmPassword.getText().toString())) {
                    dialogNotification.openDialogNotification("Mật khẩu xác thực không khớp", RegisterActivity.this);
                } else {
                    try {
                        registerApi();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
    }

    private void registerApi() throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
        binding.btnRegister.setVisibility(View.GONE);
        binding.pbLoading.setVisibility(View.VISIBLE);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "");
        jsonObject.put("password", binding.textPassword);
        jsonObject.put("name", binding.textName);
        jsonObject.put("phone", "");
        jsonObject.put("email", binding.textEmail);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, base_url + "/register", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JsonObject convertedObject = new Gson().fromJson(response.getString("data"), JsonObject.class);
                    Log.d("ABC", convertedObject.toString());
                    String message = response.getString("message");

                    signUp(); // sign up on firebase

                    //dialogNotification.openDialogNotification(message.substring(0, message.length() - 1), RegisterActivity.this);

                } catch (JSONException e) {
                    binding.btnRegister.setVisibility(View.VISIBLE);
                    binding.pbLoading.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
//                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                binding.btnRegister.setVisibility(View.VISIBLE);
                binding.pbLoading.setVisibility(View.GONE);
                if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), RegisterActivity.this);
                        Log.d("ABC", message);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void signUp() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Program.KEY_NAME, binding.textName.getText().toString());
        user.put(Program.KEY_EMAIL, binding.textEmail.getText().toString());
        user.put(Program.KEY_PASSWORD, binding.textPassword.getText().toString());
//            user.put(Program.KEY_IMAGE, encodeImage);
        database.collection(Program.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    binding.btnRegister.setVisibility(View.VISIBLE);
                    binding.pbLoading.setVisibility(View.GONE);
                    preferenceManager.putBoolean(Program.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Program.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Program.KEY_NAME, binding.textName.getText().toString());

                    Toast.makeText(RegisterActivity.this, "Success!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

//                        preferenceManager.putString(Program.KEY_IMAGE, encodeImage);
                });
    }
}