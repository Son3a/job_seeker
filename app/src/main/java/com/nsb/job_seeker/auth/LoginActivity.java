package com.nsb.job_seeker.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.BuildConfig;
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

    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    private BeginSignInRequest signInRequest;
    private SignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        Log.d("AppID", BuildConfig.APPLICATION_ID );

        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.getBoolean(Program.KEY_IS_SIGNED_IN)) {

            Program.idSavedJobs = preferenceManager.getArray(Program.LIST_SAVED_JOB);
            redirectAfterLogin(preferenceManager.getString(Program.ROLE));
        }
        initGoogle();

        getToken();

        this.loadingDialog = new LoadingDialog(LoginActivity.this);

        setEvent();
    }

    private void setEvent() {

        clickLogin();
        clickRegister();
        clickForgotPassword();

        //clickLoginGoogle();
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
                        .setFilterByAuthorizedAccounts(true)
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
                        postLogin(binding.textEmail.getText().toString().trim(), binding.textPassword.getText().toString().trim());
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

    private void postLogin(String email, String password) throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(LoginActivity.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", email);
        jsonObject.put("password", password);
        jsonObject.put("tokenDevice", tokenDevice);

        binding.btnLogin.setVisibility(View.GONE);
        binding.pbLoading.setVisibility(View.VISIBLE);

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
                    binding.btnLogin.setVisibility(View.VISIBLE);
                    binding.pbLoading.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                binding.btnLogin.setVisibility(View.VISIBLE);
                binding.pbLoading.setVisibility(View.GONE);
                if (error.networkResponse != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        Toast.makeText(LoginActivity.this, message.substring(1, message.length() - 1), Toast.LENGTH_SHORT).show();
                    } catch (UnsupportedEncodingException e) {
                        Log.d("ABC", e.toString());
                        e.printStackTrace();
                    }
                }
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
                    binding.btnLogin.setVisibility(View.VISIBLE);
                    binding.pbLoading.setVisibility(View.GONE);

                    Program.idSavedJobs = new ArrayList<>();
                    if (role.equals(Program.ADMIN_ROLE)) {
                        preferenceManager.putString(Program.COMPANY_ID, response.getJSONObject("company").getString("_id"));
                    } else {
                        JSONArray listJobFavorite = response.getJSONArray("jobFavourite");
                        for (int i = 0; i < listJobFavorite.length(); i++) {
                            if (!listJobFavorite.getJSONObject(i).isNull("jobId")) {
                                JSONObject jobObject = listJobFavorite.getJSONObject(i).getJSONObject("jobId");
                                if (jobObject.getString("status").equals("true")) {
                                    Program.idSavedJobs.add(jobObject.getString("_id"));
                                }
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