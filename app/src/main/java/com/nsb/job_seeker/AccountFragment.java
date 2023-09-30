package com.nsb.job_seeker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.auth.Activity_ChangePassword;
import com.nsb.job_seeker.auth.DialogNotification;
import com.nsb.job_seeker.auth.LoadingDialog;
import com.nsb.job_seeker.auth.MainActivity;
import com.nsb.job_seeker.auth.Activity_Profile;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.employer.StatisticalJobActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    TextView tvLogin, tvChangePw, tvMyFile, tvDeleteAccount, tvRegister, tvStatistical;
    private View beforeLoginView, afterLoginView;
    private LinearLayout layoutManager;
    private Button btnLogout;

    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private String base_url = Program.url_dev + "/auth";
    private RequestQueue mRequestQueue;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        beforeLoginView = inflater.inflate(R.layout.fragment_account_before_login, container, false);
        afterLoginView = inflater.inflate(R.layout.fragment_account_after_login, container, false);

        setControl();
        setEvent();

//        if(Program.isLogin == false){
//           return beforeLoginView;
//        }
        return afterLoginView;
    }

    private void setControl() {
        this.loadingDialog = new LoadingDialog(getActivity());
        tvLogin = beforeLoginView.findViewById(R.id.tv_login);
        tvRegister = beforeLoginView.findViewById(R.id.tv_register);

        btnLogout = afterLoginView.findViewById(R.id.btn_logout);
        tvChangePw = afterLoginView.findViewById(R.id.tv_change_password);
        tvMyFile = afterLoginView.findViewById(R.id.tv_my_file);
        tvDeleteAccount = afterLoginView.findViewById(R.id.tv_delete_account);
        tvStatistical = afterLoginView.findViewById(R.id.tv_statistical);
        preferenceManager = new PreferenceManager(getActivity());
        layoutManager = afterLoginView.findViewById(R.id.layout_manager);
    }

    private void setEvent() {
        if (preferenceManager.getString(Program.ROLE).equals(Program.USER_ROLE)) {
            layoutManager.setVisibility(View.GONE);
        }

        tvStatistical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), StatisticalJobActivity.class);
                startActivity(i);
            }
        });

        tvChangePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Activity_ChangePassword.class);
                startActivity(i);
            }
        });
        tvMyFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Activity_Profile.class);
                startActivity(i);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.startLoadingDialog();
                Log.d("Token", preferenceManager.getString(Program.TOKEN));
                handleLogout();
            }
        });
    }

    private void handleLogout() {
        mRequestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Program.url_dev + "/auth/logout", null, new Response.Listener<JSONObject>() {
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
                        if (error.networkResponse.statusCode == 401) {
                            Toast.makeText(getActivity(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getActivity(), MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), getActivity());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                loadingDialog.dismissDialog();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
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
                Log.d("Error", error.getMessage());
                if (error.networkResponse.data != null) {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(getActivity(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getActivity(), MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        preferenceManager.clear();
                        startActivity(i);
                    }
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Program.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Program.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Program.KEY_FCM_TOKEN, null);
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
//                    startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
                    preferenceManager.clear();
                    Toast.makeText(getActivity(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getContext(), MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    Log.d("Error", e.getMessage());
                    preferenceManager.clear();
                    Toast.makeText(getActivity(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getContext(), MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                });
    }
}
