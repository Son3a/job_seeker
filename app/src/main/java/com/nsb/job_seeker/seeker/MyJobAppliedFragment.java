package com.nsb.job_seeker.seeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.auth.MainActivity;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyJobAppliedFragment extends Fragment implements JobListener {
    private MyJobAppliedFragment myJobAppliedFragment;
    private View appliedJobView;
    private ProgressBar pbLoading;
    private List<Job> jobListApplied;
    private RecyclerView listView;
    private TextView tvNotify;
    private PreferenceManager preferenceManager;
    private JobAdapter jobAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        appliedJobView = inflater.inflate(R.layout.fragment_seeker_applied_job, container, false);

        setControl();
        setEvent();
        return appliedJobView;
    }

    private void setControl() {
        pbLoading = appliedJobView.findViewById(R.id.idLoadingPB);
        jobListApplied = new ArrayList<>();
        listView = appliedJobView.findViewById(R.id.lv_job_applied);
        tvNotify = appliedJobView.findViewById(R.id.tv_notify);

        preferenceManager = new PreferenceManager(getActivity());

        jobAdapter = new JobAdapter(jobListApplied, this, false);
        listView.setAdapter(jobAdapter);
    }

    private void setEvent() {
        getJobApplied();
    }

    private void getJobApplied() {
        String url = "https://job-seeker-smy5.onrender.com/application/get-by-userid";
        String token = preferenceManager.getString(Program.TOKEN);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        pbLoading.setVisibility(View.VISIBLE);
        JsonObjectRequest data = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String idCompany = "";
                            JSONArray jobsList = response.getJSONObject("data").getJSONArray("data");
                            String timeApply = "";
                            for (int i = 0; i < jobsList.length(); i++) {
                                timeApply = formatTimeApply(jobsList.getJSONObject(i).getString("submitDate"));
                                JSONObject job = jobsList.getJSONObject(i).getJSONObject("idJob");
                                if (!job.isNull("idCompany")) {
                                    idCompany = job.getJSONObject("idCompany").getString("name");
                                } else {
                                    idCompany = "";
                                }
                                jobListApplied.add(new Job(
                                        job.getString("_id"),
                                        job.getString("name"),
                                        idCompany,
                                        job.getString("locationWorking"),
                                        job.getString("salary"),
                                        "Ứng tuyển vào " + timeApply
                                ));

                            }
                            if (jobsList.length() == 0) {
                                tvNotify.setVisibility(View.VISIBLE);
                            } else {
                                jobAdapter.notifyDataSetChanged();
                            }
                            pbLoading.setVisibility(View.GONE);
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
                        if (error.networkResponse.statusCode == 401 && error.networkResponse.data != null) {
                            Toast.makeText(getActivity(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getActivity(), MainActivity.class);
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
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
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

            }
        });
        queue.add(data);
    }

    private String formatTimeApply(String timeApply) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("HH:mm dd-MM-yyyy");

        String create = output.format(sdf.parse(timeApply));
        System.out.println(create);
        return create;
    }

    @Override
    public void onClick(Job job) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", false);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, int position, boolean isSaveView, ListViewItemJobBinding binding) {

    }
}
