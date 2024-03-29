package com.nsb.job_seeker.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.activity.Activity_ChangePassword;
import com.nsb.job_seeker.activity.Activity_Profile;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentAccountBinding;
import com.nsb.job_seeker.room.KeywordDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private LoadingDialog loadingDialog;
    private String base_url = Constant.url_dev + "/auth";
    private PreferenceManager preferenceManager;
    private int ACTIVITY_PROFILE = 1;
    private final int RESULT_OK = -1;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent.getStringExtra(Constant.AVATAR) != null) {
                            String avatar = intent.getStringExtra(Constant.AVATAR);
                            binding.imageAvatar.setImageBitmap(Constant.getBitmapFromEncodedString(avatar));
                        }
                        String name = intent.getStringExtra(Constant.NAME);
                        binding.textName.setText(name);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
        if (getActivity() != null) {
            loadingDialog = new LoadingDialog(getActivity());
            preferenceManager = new PreferenceManager(getActivity());
        }
    }

    private void setEvent() {
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            loadInfo();
            clickChangePW();
            clickInfo();
            clickLogout();
        } else {
            binding.layoutProfile.setVisibility(View.GONE);
            binding.layoutLogin.setVisibility(View.VISIBLE);
            binding.buttonLogout.setVisibility(View.GONE);
            binding.buttonLogin.setVisibility(View.VISIBLE);
            gotoAppWithoutLogin();
        }
    }

    private void gotoAppWithoutLogin() {
        binding.buttonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadInfo() {
        if (preferenceManager.getString(Constant.AVATAR) != null) {
            if (URLUtil.isValidUrl(preferenceManager.getString(Constant.AVATAR))) {
                Picasso.get().load(preferenceManager.getString(Constant.AVATAR)).into(binding.imageAvatar);
            } else {
                binding.imageAvatar.setImageBitmap(Constant.getBitmapFromEncodedString(preferenceManager.getString(Constant.AVATAR)));
            }
        }
        binding.textName.setText(preferenceManager.getString(Constant.NAME));
    }

    private void clickChangePW() {
        binding.textChangePW.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), Activity_ChangePassword.class);
            startActivity(i);
        });
    }

    private void clickLogout() {
        binding.buttonLogout.setOnClickListener(v -> {
            Log.d("Token", preferenceManager.getString(Constant.TOKEN));
            handleLogout();
        });
    }

    private void clickInfo() {
        binding.layoutInfo.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), Activity_Profile.class);
            activityResultLauncher.launch(i);
        });
    }

    private void handleLogout() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity());
        loadingDialog.showDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Constant.url_dev + "/auth/logout", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("ABC", "Monggo: Sign out successfully!");
                KeywordDatabase.getInstance(getContext()).keywordDAO().deleteAll();
                signOut();
                //signOut();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.getMessage());
                loadingDialog.hideDialog();
                String body;
                //get status code here
                NetworkResponse statusCode = error.networkResponse;
                if (statusCode.data != null) {
                    try {
                        if (error instanceof com.android.volley.NoConnectionError) {

                        } else if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(getActivity(), LoginActivity.class);
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

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constant.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constant.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constant.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    loadingDialog.hideDialog();
//                    startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
                    preferenceManager.clear();
                    Toast.makeText(getActivity(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.hideDialog();
                    Log.d("Error", e.getMessage());
                    preferenceManager.clear();
                    Toast.makeText(getActivity(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                });
    }

}
