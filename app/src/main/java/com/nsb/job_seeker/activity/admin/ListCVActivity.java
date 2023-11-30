package com.nsb.job_seeker.activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.adapter.CVAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityEmployerListCvBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListCVActivity extends BaseActivity {
    private ActivityEmployerListCvBinding binding;
    private ArrayList<String> listTimeApply;
    private ArrayList<String> listFile;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityEmployerListCvBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();
        setEvent();
    }

    private void setControl() {
        listTimeApply = new ArrayList<String>();
        listFile = new ArrayList<>();
        loadingDialog = new LoadingDialog(this);
        preferenceManager = new PreferenceManager(this);
    }

    private void setEvent() {


        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getListCV();
    }

    private void setListAdapter() {
        CVAdapter cvAdapter = new CVAdapter(ListCVActivity.this, R.layout.list_view_item_cv, listTimeApply, listFile);
        binding.lvCv.setAdapter(cvAdapter);
        binding.lvCv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void getListCV() {
        Bundle bundle = getIntent().getExtras();
        String url = Constant.url_dev + "/application/get-by-jobid?jobid=" + bundle.getString(Constant.JOB_ID);
        loadingDialog.showDialog();
        RequestQueue queue = Volley.newRequestQueue(ListCVActivity.this);

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadingDialog.hideDialog();
                            JSONArray listCV = response.getJSONObject("data").getJSONArray("data");
                            for (int i = 0; i < listCV.length(); i++) {
                                JSONObject CV = listCV.getJSONObject(i);

                                listFile.add(CV.getString("cv"));
                                listTimeApply.add(Constant.formatTimeDDMMYYYY(CV.getString("submitDate")));
                            }
                            if (listFile.size() == 0) {
                                binding.tvNotifyEmpty.setVisibility(View.VISIBLE);
                            } else {
                                setListAdapter();
                            }
                            binding.tvAmount.setText(String.valueOf(listFile.size()));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof com.android.volley.NoConnectionError) {

                        } else if (error.networkResponse != null) {
                            if (error.networkResponse.statusCode == 401) {
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                preferenceManager.clear();
                                startActivity(i);
                            }
                        }
                        System.out.println(error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", preferenceManager.getString(Constant.TOKEN));
                return headers;
            }
        };
        ;
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
