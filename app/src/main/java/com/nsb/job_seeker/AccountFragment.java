package com.nsb.job_seeker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.auth.Activity_ChangePassword;
import com.nsb.job_seeker.auth.DialogNotification;
import com.nsb.job_seeker.auth.LoadingDialog;
import com.nsb.job_seeker.auth.MainActivity;
import com.nsb.job_seeker.common.Activity_Profile;
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
                handleLogout(preferenceManager.getString(Program.TOKEN));
            }
        });
    }

    private void handleLogout(String accessToken) {
        mRequestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Program.url_dev + "/auth/logout", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    Log.d("ABC", message);

                    preferenceManager.clear();

                    Intent i = new Intent(getContext(), MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } catch (JSONException e) {
                    Log.d("ABC", e.toString());
                    e.printStackTrace();
                }
                loadingDialog.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here

                if (error.networkResponse.statusCode == 401) {
                    handleLogout(preferenceManager.getString(Program.REFRESH_TOKEN));
                } else if (error.networkResponse.data != null) {
                    try {
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
                params.put("Authorization", accessToken);
                return params;
            }
        };
        mRequestQueue.add(jsonObjectRequest);
    }
}
