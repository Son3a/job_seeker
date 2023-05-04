package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyJobSavedFragment extends Fragment {
    private MyJobSavedFragment myJobSavedFragment;
    private View savedJobView;
    private ListView listViewJobSaved;
    private ProgressBar pbLoading;

    private List<Job> jobList;

    private ListViewApdapter listViewApdapter;

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

        jobList = new ArrayList<>();
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
                                JSONObject job = jobsList.getJSONObject(i).getJSONObject("jobId");
                                Log.d("ABC", job.getString("name"));
                                if (job.getString("status").equals("true")) {
                                    if (!job.isNull("idCompany")) {
                                        idCompany = job.getJSONObject("idCompany").getString("name");
                                    } else {
                                        idCompany = "";
                                    }

                                    String time = Program.setTime(job.getString("updateDate"));
                                    if (time.equals(null))
                                        time = "Vừa mới cập nhật";
                                    else
                                        time = "Cập nhật " + time + " trước";
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
                            setListViewAdapter();
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
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", Program.token);
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

            }
        });
        queue.add(data);
    }


    private void setListViewAdapter() {
        listViewApdapter = new ListViewApdapter(getActivity(), R.layout.list_view_item_job, jobList, true, true);
        listViewJobSaved.setAdapter(listViewApdapter);
        listViewJobSaved.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
