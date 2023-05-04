package com.nsb.job_seeker.seeder;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ForMeFragment extends Fragment {

    private ListViewApdapter listViewApdapter;
    private ListView listViewJob;
    private String url = "https://job-seeker-smy5.onrender.com/job/list";
    private View homeView;
    private ProgressBar pbLoading;

    public static List<Job> jobList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeView = inflater.inflate(R.layout.fragment_seeker_for_me, container, false);

        setControl();
        if (ForMeFragment.jobList.isEmpty()) {
            callAPI(url);
        }

        return homeView;
    }

    private void setControl() {
        jobList = new ArrayList<Job>();
        listViewJob = homeView.findViewById(R.id.lv_job);
        pbLoading=homeView.findViewById(R.id.idLoadingPB);
    }

    private void callAPI(String url) {
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
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (!job.isNull("idCompany")) {
                                    idCompany = job.getJSONObject("idCompany").getString("name");
                                } else {
                                    idCompany = "";
                                }

                                jobList.add(new Job(
                                        job.getString("_id"),
                                        job.getString("name"),
                                        idCompany,
                                        job.getString("locationWorking"),
                                        job.getString("salary"),
                                        Program.setTime(job.getString("postingDate"))
                                ));

                            }
                            pbLoading.setVisibility(View.GONE);
                            setEvent();
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
                        System.out.println(error);
                    }
                }
        );
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

    private void setEvent() {
        listViewApdapter = new ListViewApdapter(getActivity(), R.layout.list_view_item_job, jobList,true);
        listViewJob.setAdapter(listViewApdapter);
        listViewJob.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(getActivity(), JobDetailActivity.class);
                i.putExtra("id", jobList.get(position).getId());
                i.putExtra("isApply", true);
                startActivity(i);
            }
        });
    }
}
