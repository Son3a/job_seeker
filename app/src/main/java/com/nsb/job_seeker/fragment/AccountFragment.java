package com.nsb.job_seeker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.Activity_ChangePassword;
import com.nsb.job_seeker.activity.DialogNotification;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.Activity_Profile;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    TextView textChangePw, textRemoveAccount;
    private View view;
    private ConstraintLayout layoutInfo;
    private MaterialButton btnLogout;

    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private String base_url = Constant.url_dev + "/auth";
    private RequestQueue mRequestQueue;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

        setControl();
        setEvent();

        return view;
    }

    private void setControl() {
        this.loadingDialog = new LoadingDialog(getActivity());
        textChangePw = view.findViewById(R.id.textChangePW);
        textRemoveAccount = view.findViewById(R.id.textRemoveAccount);
        layoutInfo = view.findViewById(R.id.layoutInfo);
        btnLogout = view.findViewById(R.id.buttonLogout);

        preferenceManager = new PreferenceManager(getActivity());
    }

    private void setEvent() {
        clickChangePW();

        clickInfo();

        clickLogout();
    }

    private void clickChangePW() {
        textChangePw.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), Activity_ChangePassword.class);
            startActivity(i);
        });
    }

    private void clickLogout() {
        btnLogout.setOnClickListener(v -> {
            Log.d("Token", preferenceManager.getString(Constant.TOKEN));
            handleLogout();
        });
    }

    private void clickInfo() {
        layoutInfo.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), Activity_Profile.class);
            startActivity(i);
        });
    }

    private void handleLogout() {
        mRequestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Constant.url_dev + "/auth/logout", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("ABC", "Monggo: Sign out successfully!");
                signOut();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.getMessage());
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

                            dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), getActivity());
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
                Log.d("Error", error.getMessage());
                if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
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
        updates.put(Constant.KEY_FCM_TOKEN, null);
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
//                    startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
                    preferenceManager.clear();
                    Toast.makeText(getActivity(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    Log.d("Error", e.getMessage());
                    preferenceManager.clear();
                    Toast.makeText(getActivity(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                });
    }
}
