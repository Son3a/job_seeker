package com.nsb.job_seeker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.nsb.job_seeker.employer.StatisticalJobActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    TextView tvLogin, tvLogout, tvChangePw, tvMyFile, tvDeleteAccount, tvRegister, tvStatistical;
    private View beforeLoginView, afterLoginView;

    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;
    private String base_url = Program.url_dev+"/auth";
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;

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

        tvLogout = afterLoginView.findViewById(R.id.tv_logout);
        tvChangePw = afterLoginView.findViewById(R.id.tv_change_password);
        tvMyFile = afterLoginView.findViewById(R.id.tv_my_file);
        tvDeleteAccount = afterLoginView.findViewById(R.id.tv_delete_account);
        tvStatistical = afterLoginView.findViewById(R.id.tv_statistical);
    }

    private void setEvent() {
        if(Program.role.equals("user")){
            tvStatistical.setVisibility(View.GONE);
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

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.startLoadingDialog();
                handleLogout();
            }
        });
    }

    private void handleLogout() {
        mRequestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Program.url_dev + "/auth/logout", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    Log.d("ABC", message);

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Program.sharedPreferencesName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.commit();

                    Program.idCompany = null;
                    Program.idUser = null;
                    Program.role = null;
                    Program.token = null;

                    Intent i = new Intent(getContext(), MainActivity.class);
                    startActivity(i);
                } catch (JSONException e) {
                    Log.d("ABC",e.toString());
                    e.printStackTrace();
                }
                loadingDialog.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here

                if(error.networkResponse.data!=null) {
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring( 1, message.length() - 1 ), getActivity());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                loadingDialog.dismissDialog();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Program.sharedPreferencesName, Context.MODE_PRIVATE);
                String ACCESSTOKEN = sharedPreferences.getString("accessToken", "");
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer "+ACCESSTOKEN.substring(1, ACCESSTOKEN.length()-1));
                return params;
            }
        };;
        mRequestQueue.add(jsonObjectRequest);
    }
}
