package com.nsb.job_seeker.seeker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.auth.LoginActivity;
import com.nsb.job_seeker.common.AsyncTasks;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyJobSavedFragment extends Fragment implements JobListener {
    private View savedJobView;
    private RecyclerView listViewJobSaved;
    private ProgressBar pbLoading;
    private TextView tvNotify;
    private List<Job> jobList;
    private PreferenceManager preferenceManager;
    private JobAdapter jobAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        savedJobView = inflater.inflate(R.layout.fragment_seeker_saved_job, container, false);

        setControl();
        setEvent();
        return savedJobView;
    }

    private void setControl() {
        listViewJobSaved = savedJobView.findViewById(R.id.lv_job_saved);
        pbLoading = savedJobView.findViewById(R.id.idLoadingPB);
        tvNotify = savedJobView.findViewById(R.id.tv_notify);

        jobList = new ArrayList<>();
        preferenceManager = new PreferenceManager(getActivity());

        jobAdapter = new JobAdapter(jobList, this, true, true);
        listViewJobSaved.setAdapter(jobAdapter);
    }

    private void setEvent() {
        getJobSaved();
    }

    private void getJobSaved() {
        String url = "https://job-seeker-smy5.onrender.com/auth/info-user";
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        pbLoading.setVisibility(View.VISIBLE);
        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String idCompany = "";
                            JSONArray jobsList = response.getJSONArray("jobFavourite");
//                            Log.d("ABC", String.valueOf(jobsList));

                            for (int i = 0; i < jobsList.length(); i++) {
                                if(jobsList.getJSONObject(i).isNull("jobId")) break;
                                JSONObject job = jobsList.getJSONObject(i).getJSONObject("jobId");
                                Log.d("ABC", job.getString("name"));
                                if (job.getString("status").equals("true")) {
                                    if (!job.isNull("idCompany")) {
                                        idCompany = job.getJSONObject("idCompany").getString("name");
                                    } else {
                                        idCompany = "";
                                    }

                                    String time = Program.setTime(job.getString("deadline"));

                                    jobList.add(new Job(
                                            job.getString("_id"),
                                            job.getString("name"),
                                            idCompany,
                                            job.getString("locationWorking"),
                                            Program.formatSalary(job.getString("salary")),
                                            time
                                    ));
                                }
                            }

                            pbLoading.setVisibility(View.GONE);
                            if (jobsList.length() == 0) {
                                tvNotify.setVisibility(View.VISIBLE);
                            } else {
                                jobAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse.data != null & error.networkResponse.statusCode == 401){
                            Toast.makeText(getActivity(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getActivity(), LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        System.out.println(error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", preferenceManager.getString(Program.TOKEN));
                headers.put("Content-Type", "application/json");
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
                if(error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 401) {

                        new AsyncTasks() {
                            @Override
                            public void onPreExecute() {
                            }

                            @Override
                            public void doInBackground() {
                                Intent i = new Intent(getActivity(), LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                preferenceManager.clear();
                                startActivity(i);
                            }

                            @Override
                            public void onPostExecute() {
                                Toast.makeText(getActivity(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    }
                }
            }
        });
        queue.add(data);
    }

    @Override
    public void onClick(Job job) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", true);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, int position, boolean isSaveView, ListViewItemJobBinding binding) {

    }
}

