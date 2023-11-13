package com.nsb.job_seeker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.BuildConfig;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.admin.EmployerMainActivity;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityLoginBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;
    private String base_url = Constant.url_dev + "/auth";
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private PreferenceManager preferenceManager;

    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    private BeginSignInRequest signInRequest;
    private SignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        Log.d("AppID", BuildConfig.APPLICATION_ID);

        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {

            Constant.idSavedJobs = preferenceManager.getArray(Constant.LIST_SAVED_JOB);
            redirectAfterLogin(preferenceManager.getString(Constant.ROLE));
        }
        initGoogle();

        this.loadingDialog = new LoadingDialog(LoginActivity.this);

        setEvent();
    }

    private void setEvent() {
        clickLogin();
        clickRegister();
        clickForgotPassword();
    }

    private void initGoogle() {
        signInClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        clickLoginGoogle();
    }

    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            SignInCredential credential = signInClient.getSignInCredentialFromIntent(result.getData());
                            String idToken = credential.getGoogleIdToken();
                            if (idToken != null) {
                                String email = credential.getId();
                                // Got an ID token from Google. Use it to authenticate
                                // with your backend.
                                Log.d("TAG", "Got ID token.");
                                Log.d("TAG", email);
                            }
                        } catch (ApiException e) {
                            // ...
                            Log.d("TAG", e.getMessage());
                        }
                    }
                }
            });

    private void clickLoginGoogle() {
        binding.imageGoogle.setOnClickListener(v -> {
            signInClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(LoginActivity.this, new OnSuccessListener<BeginSignInResult>() {
                        @Override
                        public void onSuccess(BeginSignInResult result) {
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(
                                    result.getPendingIntent().getIntentSender()).build();
                            activityResultLauncher.launch(intentSenderRequest);
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // No Google Accounts found. Just continue presenting the signed-out UI.
                            Log.d("TAG", e.getMessage());
                        }
                    });
        });
    }


    private void clickForgotPassword() {
        binding.textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, ForgotActivity.class);
                startActivity(i);
            }
        });
    }

    private void clickRegister() {
        binding.textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    private void clickLogin() {
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmpty(binding.textEmail)) {
                    binding.layoutErrorEmail.setVisibility(View.VISIBLE);
                    binding.textEmail.setBackgroundResource(R.drawable.background_edittext_error);
                } else {
                    binding.layoutErrorEmail.setVisibility(View.GONE);
                    binding.textEmail.setBackgroundResource(R.drawable.background_edittext_register);
                }
                if (isEmpty(binding.textPassword)) {
                    binding.layoutErrorPassword.setVisibility(View.VISIBLE);
                    binding.textPassword.setBackgroundResource(R.drawable.background_edittext_error);
                } else {
                    binding.layoutErrorPassword.setVisibility(View.GONE);
                    binding.textPassword.setBackgroundResource(R.drawable.background_edittext_register);
                }
                if (!isEmpty(binding.textPassword) && !isEmpty(binding.textEmail) && binding.cbxAcceptApp.isChecked()) {
                    binding.layoutErrorEmail.setVisibility(View.GONE);
                    binding.textEmail.setBackgroundResource(R.drawable.background_edittext_register);
                    binding.layoutErrorPassword.setVisibility(View.GONE);
                    binding.textPassword.setBackgroundResource(R.drawable.background_edittext_register);
                    try {
                        Constant.hideKeyboardFrom(LoginActivity.this, binding.getRoot());
                        postLogin();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    private void postLogin() throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this);

        binding.btnLogin.setVisibility(View.GONE);
        binding.pbLoading.setVisibility(View.VISIBLE);

        String email = binding.textEmail.getText().toString().trim();
        String password = binding.textPassword.getText().toString().trim();

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("email", email);
        jsonReq.put("password", password);

        JsonObjectRequest data = new JsonObjectRequest(
                Request.Method.POST,
                base_url + "/login",
                jsonReq,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response.getJSONObject("data");

                            String role = jsonObject.getString("role");
                            //signIn(email, password);   //login firebase

                            Constant.idSavedJobs = new ArrayList<>();
                            if (role.equals(Constant.ADMIN_ROLE)) {
                                preferenceManager.putString(Constant.COMPANY_ID, jsonObject.getJSONObject("idCompany").getString("_id"));
                            } else {
                                JSONArray listJobFavorite = jsonObject.getJSONArray("jobFavourite");
                                for (int i = 0; i < listJobFavorite.length(); i++) {
                                    if (!listJobFavorite.getJSONObject(i).isNull("jobId")) {
                                        JSONObject jobObject = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                                        if (jobObject.getString("status").equals("true")) {
                                            Constant.idSavedJobs.add(jobObject.getString("_id"));
                                        }
                                    }
                                }
                            }
                            String accessToken = jsonObject.get("refreshToken").toString();
                            preferenceManager.putString(Constant.TOKEN, "Bearer " + accessToken.replace("\"", ""));
                            preferenceManager.putString(Constant.NAME, jsonObject.getString("name"));
                            preferenceManager.putString(Constant.MAIL, jsonObject.getString("email"));
                            if (!jsonObject.getString("avatar").isEmpty()) {
                                preferenceManager.putString(Constant.AVATAR, jsonObject.getString("avatar"));
                                preferenceManager.putString(Constant.PHONE, jsonObject.getString("phone"));
                            }
                            preferenceManager.putString(Constant.ROLE, role);
                            preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);


                            getAppliedJob(role);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            binding.btnLogin.setVisibility(View.VISIBLE);
                            binding.pbLoading.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Error response: " + error.toString());
                        String body;
                        //get status code here
                        binding.btnLogin.setVisibility(View.VISIBLE);
                        binding.pbLoading.setVisibility(View.GONE);

                        if (error instanceof com.android.volley.NoConnectionError) {
                            CustomToast.makeText(LoginActivity.this, "Hệ thống đang có lỗi, quý khách vui lòng quay lại sau!",
                                    CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                        } else if (error instanceof AuthFailureError) {
                            try {
                                body = new String(error.networkResponse.data, "UTF-8");
                                JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                                String message = convertedObject.get("message").toString();
                                CustomToast.makeText(LoginActivity.this, message.substring(1, message.length() - 1), CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        } else if(error instanceof VolleyError){
                            CustomToast.makeText(LoginActivity.this, error.getMessage(), CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                        }
                    }
                }
        );
        data.setRetryPolicy(new RetryPolicy() {
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
                throw new VolleyError("Email hoặc mật khẩu không chính xác!");
            }
        });
        queue.add(data);
        ////////////////////////////////////////////////////////////////

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


    private void signIn(String email, String password, String role) {
        Log.d("Process", "Login Firebase");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constant.KEY_COLLECTION_USERS)
                .whereEqualTo(Constant.KEY_EMAIL, email)
                .whereEqualTo(Constant.KEY_PASSWORD, password)
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null &&
                                    task.getResult().getDocuments().size() > 0) {
                                binding.btnLogin.setVisibility(View.VISIBLE);
                                binding.pbLoading.setVisibility(View.GONE);

                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                Log.d("UserId", documentSnapshot.getId());
                                preferenceManager.putString(Constant.KEY_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Constant.KEY_NAME, documentSnapshot.getString(Constant.KEY_NAME));

                                redirectAfterLogin(role);
                                Log.d("Process", "Login Firebase successfully");
                            }
                        }
                ).addOnFailureListener(e -> {
                    Log.d("Error", e.getMessage());
                });
    }

    private void getAppliedJob(String role) {
        String url = Constant.url_dev + "/application/get-by-userid";
        String token = preferenceManager.getString(Constant.TOKEN);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest data = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Constant.idAppliedJob = new ArrayList<>();
                            JSONArray jobsList = response.getJSONObject("data").getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i).getJSONObject("idJob");
                                Constant.idAppliedJob.add(job.getString("_id"));
                            }
                            signIn(binding.textEmail.getText().toString().trim(), binding.textPassword.getText().toString().trim(), role);
                        } catch (JSONException e) {
                            binding.btnLogin.setVisibility(View.VISIBLE);
                            binding.pbLoading.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        binding.btnLogin.setVisibility(View.VISIBLE);
                        binding.pbLoading.setVisibility(View.GONE);
                        if (error instanceof com.android.volley.NoConnectionError) {

                        } else if (error.networkResponse.statusCode == 401 && error.networkResponse.data != null) {
                            Intent i = new Intent(LoginActivity.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };
        data.setRetryPolicy(new RetryPolicy() {
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
        queue.add(data);
    }

}