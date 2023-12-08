package com.nsb.job_seeker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private String base_url = Constant.url_dev + "/auth";
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    private BeginSignInRequest signInRequest;
    private SignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        init();
        setEvent();
    }

    private void init() {
        preferenceManager = new PreferenceManager(this);
        loadingDialog = new LoadingDialog(this);
        initGoogle();
    }

    private void setEvent() {
        clickLogin();
        clickRegister();
        clickForgotPassword();
        gotoAppWithoutLogin();
    }

    private void gotoAppWithoutLogin() {
        binding.textVisitPage.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeekerMainActivity.class);
            preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
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
                                String password = credential.getPassword();
                                // Got an ID token from Google. Use it to authenticate
                                // with your backend.
                                Log.d("Google", "Got ID token.");
                                Log.d("Google", "name: " + credential.getDisplayName());
                                loginGoogle(credential.getDisplayName(), email, credential.getProfilePictureUri().toString());
                            }
                        } catch (ApiException e) {
                            Log.d("Google", e.getMessage());
                            CustomToast.makeText(LoginActivity.this, e.getMessage(), CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void clickLoginGoogle() {
        binding.imageGoogle.setOnClickListener(v -> {
            loadingDialog.showDialog();
            signInClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(LoginActivity.this, new OnSuccessListener<BeginSignInResult>() {
                        @Override
                        public void onSuccess(BeginSignInResult result) {
                            loadingDialog.hideDialog();
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(
                                    result.getPendingIntent().getIntentSender()).build();
                            activityResultLauncher.launch(intentSenderRequest);
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // No Google Accounts found. Just continue presenting the signed-out UI.
                            loadingDialog.hideDialog();
                            CustomToast.makeText(LoginActivity.this, e.getMessage(), CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                            Log.d("Google", e.getMessage());
                        }
                    });
        });
    }

    private void signInFireBase(String id, String name, String email, String avatar, String role) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constant.KEY_COLLECTION_USERS)
                .whereEqualTo(Constant.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null &&
                                    task.getResult().getDocuments().size() > 0) {
                                loadingDialog.hideDialog();
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                Log.d("UserId", documentSnapshot.getId());
                                preferenceManager.putString(Constant.KEY_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Constant.KEY_NAME, documentSnapshot.getString(Constant.KEY_NAME));
                                preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                                redirectToApp(role);

                            } else {
                                HashMap<String, Object> user = new HashMap<>();
                                user.put(Constant.KEY_USER_ID, id);
                                user.put(Constant.KEY_NAME, name);
                                user.put(Constant.KEY_EMAIL, email);
                                user.put(Constant.KEY_IMAGE, avatar);
                                user.put(Constant.KEY_PASSWORD, Constant.PASSWORD_DEFAULT);
                                user.put(Constant.KEY_AVAILABILITY, 0);
                                user.put(Constant.KEY_FCM_TOKEN, "");
                                database.collection(Constant.KEY_COLLECTION_USERS)
                                        .add(user)
                                        .addOnSuccessListener(documentReference -> {
                                            loadingDialog.hideDialog();
                                            CustomToast.makeText(LoginActivity.this, "Đăng nhập thành công!",
                                                    CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();

                                            preferenceManager.putString(Constant.KEY_USER_ID, documentReference.getId());
                                            preferenceManager.putString(Constant.KEY_NAME, name);
                                            preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                                            redirectToApp(role);
                                        });
                            }
                        }
                ).addOnFailureListener(e -> {
                    Log.d("Error", "Login firebase fail " + e.getMessage());
                });
    }

    private void loginGoogle(String name, String email, String avatar) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this);

        loadingDialog.showDialog();

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("email", email);
        jsonReq.put("password", Constant.PASSWORD_DEFAULT);
        jsonReq.put("name", name);
        jsonReq.put("avatar", avatar);

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.POST, base_url + "/login-with-google", jsonReq, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("data").getJSONObject("user");
                    String role = jsonObject.getString("role");

                    Constant.idSavedJobs = new ArrayList<>();
                    if (role.equals(Constant.ADMIN_ROLE)) {
                        preferenceManager.putString(Constant.COMPANY_ID, jsonObject.getJSONObject("idCompany").getString("_id"));
                    } else {
                        JSONArray listJobFavorite = jsonObject.getJSONArray("jobFavourite");
                        for (int i = 0; i < listJobFavorite.length(); i++) {
                            if (!listJobFavorite.getJSONObject(i).isNull("jobId")) {
                                JSONObject jobObject = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                                Constant.idSavedJobs.add(jobObject.getString("_id"));
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

                    Constant.idAppliedJob = new ArrayList<>();
                    if (!response.getJSONObject("data").isNull("listCV")) {
                        JSONArray jsonCV = response.getJSONObject("data").getJSONArray("listCV");
                        for (int i = 0; i < jsonCV.length(); i++) {
                            if (!jsonCV.getJSONObject(i).isNull("idJob")) {
                                JSONObject job = jsonCV.getJSONObject(i).getJSONObject("idJob");
                                Constant.idAppliedJob.add(job.getString("_id"));
                            }
                        }
                    }
                    signInFireBase(jsonObject.getString("_id").toString(), name, email, avatar, role);

                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingDialog.hideDialog();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Error response: " + error.toString());
                        String body;
                        //get status code here
                        loadingDialog.hideDialog();

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
                        } else if (error instanceof VolleyError) {
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

        loadingDialog.showDialog();

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
                            JSONObject jsonObject = response.getJSONObject("data").getJSONObject("user");
                            String role = jsonObject.getString("role");

                            Constant.idSavedJobs = new ArrayList<>();
                            if (role.equals(Constant.ADMIN_ROLE)) {
                                preferenceManager.putString(Constant.COMPANY_ID, jsonObject.getJSONObject("idCompany").getString("_id"));
                            } else {
                                JSONArray listJobFavorite = jsonObject.getJSONArray("jobFavourite");
                                for (int i = 0; i < listJobFavorite.length(); i++) {
                                    if (!listJobFavorite.getJSONObject(i).isNull("jobId")) {
                                        JSONObject jobObject = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                                        Constant.idSavedJobs.add(jobObject.getString("_id"));
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

                            Constant.idAppliedJob = new ArrayList<>();
                            if (!response.getJSONObject("data").isNull("listCV")) {
                                JSONArray jsonCV = response.getJSONObject("data").getJSONArray("listCV");
                                for (int i = 0; i < jsonCV.length(); i++) {
                                    if (!jsonCV.getJSONObject(i).isNull("idJob")) {
                                        JSONObject job = jsonCV.getJSONObject(i).getJSONObject("idJob");
                                        Constant.idAppliedJob.add(job.getString("_id"));
                                    }
                                }
                            }

                            signIn(binding.textEmail.getText().toString().trim(), binding.textPassword.getText().toString().trim(), role);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            loadingDialog.hideDialog();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Error response: " + error.toString());
                        String body;
                        //get status code here
                        loadingDialog.hideDialog();

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
                        } else if (error instanceof VolleyError) {
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

    private void redirectToApp(String role) {
        if (role.trim().equals("user")) {
            Log.d("ABC", "user");
            Intent intent = new Intent(this, SeekerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.d("ABC", "admin");
            Intent intent = new Intent(this, EmployerMainActivity.class);
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
                                loadingDialog.hideDialog();
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                Log.d("UserId", documentSnapshot.getId());
                                preferenceManager.putString(Constant.KEY_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Constant.KEY_NAME, documentSnapshot.getString(Constant.KEY_NAME));
                                preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                                redirectToApp(role);
                                Log.d("Process", "Login Firebase successfully");
                            }
                        }
                ).addOnFailureListener(e -> {
                    preferenceManager.clear();
                    loadingDialog.hideDialog();
                    CustomToast.makeText(LoginActivity.this, e.getMessage(), CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                    Log.d("Error", e.getMessage());
                });
    }
}