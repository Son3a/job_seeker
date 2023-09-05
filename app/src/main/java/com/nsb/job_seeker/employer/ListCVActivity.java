package com.nsb.job_seeker.employer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.CVAdapter;
import com.nsb.job_seeker.auth.MainActivity;
import com.nsb.job_seeker.common.AsyncTasks;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.model.RssObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListCVActivity extends AppCompatActivity {
    private ListView listViewCV;
    private ArrayList<String> listTimeApply;
    private ArrayList<String> listFile;
    private ImageView icBack;
    private TextView tvNotifyEmpty, tvAmountJob;
    private String url = "https://job-seeker-smy5.onrender.com/application/get-by-jobid?jobid=";
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_employer_list_cv);

        setControl();
        setEvent();
    }

    private void setControl() {
        icBack = findViewById(R.id.ic_back);
        listViewCV = findViewById(R.id.lv_cv);
        tvNotifyEmpty = findViewById(R.id.tv_notify_empty);
        tvAmountJob = findViewById(R.id.tv_amount_cv);

        listTimeApply = new ArrayList<String>();
        listFile = new ArrayList<>();

        preferenceManager = new PreferenceManager(this);
    }

    private void setEvent() {


        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getListCV();
    }

    private void setListAdapter() {
        CVAdapter cvAdapter = new CVAdapter(ListCVActivity.this, R.layout.list_view_item_cv, listTimeApply, listFile);
        listViewCV.setAdapter(cvAdapter);
        listViewCV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void getListCV() {
        Bundle bundle = getIntent().getExtras();
        url += bundle.getString("id");

        RequestQueue queue = Volley.newRequestQueue(ListCVActivity.this);

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray listCV = response.getJSONObject("data").getJSONArray("data");
                            for (int i = 0; i < listCV.length(); i++) {
                                JSONObject CV = listCV.getJSONObject(i);

                                listFile.add(CV.getString("cv"));
                                listTimeApply.add(Program.formatTimeDDMMYYYY(CV.getString("submitDate")));
                            }
                            if (listFile.size() == 0) {
                                tvNotifyEmpty.setVisibility(View.VISIBLE);
                            } else {
                                setListAdapter();
                            }
                            tvAmountJob.setText("Tổng số lượng hồ sơ: " + String.valueOf(listFile.size()));
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
                        if(error.networkResponse != null) {
                            if (error.networkResponse.statusCode == 401) {
                                Toast.makeText(getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
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
                headers.put("Authorization", preferenceManager.getString(Program.TOKEN));
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
                if(error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 401) {

                        new AsyncTasks() {
                            @Override
                            public void onPreExecute() {
                            }

                            @Override
                            public void doInBackground() {
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                preferenceManager.clear();
                                startActivity(i);
                            }

                            @Override
                            public void onPostExecute() {
                                Toast.makeText(getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    }
                }
            }
        });
        queue.add(data);
    }
}
