package com.nsb.job_seeker.seeker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CompanyFragment extends Fragment implements JobListener {
    private View view;
    private TextView textNameCompany, textAddress, textIntroduce;
    private List<Job> listJobCompany;
    private JobAdapter jobAdapter;
    private RecyclerView rcvRelatedJob;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_company, container, false);

        init();
        setEvent();

        return view;
    }

    private void init() {
        listJobCompany = new ArrayList<>();
        textAddress = view.findViewById(R.id.textPosition);
        textNameCompany = view.findViewById(R.id.textNameCompany);
        textIntroduce = view.findViewById(R.id.textIntroduce);
        rcvRelatedJob = view.findViewById(R.id.rcvJobCompany);
        jobAdapter = new JobAdapter(listJobCompany, this, true);
        rcvRelatedJob.setAdapter(jobAdapter);
    }

    private void setEvent() {
        getInfoCompany();
        getListJobOfCompany();
    }

    private void getInfoCompany() {
//        pbLoading.setVisibility(View.VISIBLE);

        String urlCompany = "https://job-seeker-smy5.onrender.com/company/detail?id=" + getArguments().getString(Program.COMPANY_ID);

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                urlCompany,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response.getJSONObject("data");
                            textNameCompany.setText(jsonObject.getString("name"));
                            textIntroduce.setText(jsonObject.getString("about"));
                            textAddress.setText(jsonObject.getString("location"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //pbLoading.setVisibility(View.GONE);
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

    private void getListJobOfCompany() {
        String url = "https://job-seeker-smy5.onrender.com/job/list/company/";
        url += getArguments().getString(Program.COMPANY_ID);

        Log.d("CompanyId", getArguments().getString(Program.COMPANY_ID));

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String idCompany = "";
                            JSONArray jobsList = response.getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (job.getString("status").equals("true")) {
                                    String time = Program.setTime(job.getString("deadline"));

                                    if (time.equals(null)) {
                                        time = "Hết hạn";
                                    } else {
                                        time = "Còn " + time;
                                    }
                                    listJobCompany.add(new Job(
                                            job.getString("_id"),
                                            job.getString("name"),
                                            idCompany,
                                            job.getString("locationWorking"),
                                            job.getString("salary"),
                                            time
                                    ));
                                }
                            }
                            jobAdapter.notifyDataSetChanged();

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

    @Override
    public void onClick(Job job) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("id", job);

        Intent intent = new Intent(getActivity(), JobDetailActivity.class);
        intent.putExtras(bundle);
        getActivity().finish();
        startActivity(intent);
    }

    @Override
    public void onSave(Job job, int position, boolean isSaveView, ListViewItemJobBinding binding) {

    }
}
