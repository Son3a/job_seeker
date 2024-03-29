package com.nsb.job_seeker.fragment.seeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.databinding.FragmentCompanyBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Company;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CompanyFragment extends Fragment implements JobListener {
    private FragmentCompanyBinding binding;

    private List<Job> listJobCompany;
    private JobAdapter jobAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCompanyBinding.inflate(getLayoutInflater());

        init();
        setEvent();

        return binding.getRoot();
    }

    private void init() {
        listJobCompany = new ArrayList<>();

        jobAdapter = new JobAdapter(getContext(), listJobCompany, this, true);
        binding.rcvJobCompany.setAdapter(jobAdapter);
    }

    private void setEvent() {
        getInfoCompany();
        getListJobOfCompany();
        clickLinkCompany();
    }

    private void getInfoCompany() {
        Company company = (Company) getArguments().getSerializable(Constant.COMPANY_MODEL);
        binding.textNameCompany.setText(company.getName());
        binding.textPosition.setText(company.getAddress());
        binding.textLink.setText(company.getLink());
        binding.textIntroduce.setText(company.getAbout());
    }

    private void getListJobOfCompany() {
        String url = Constant.url_dev + "/job/list/company/";

        Company company = (Company) getArguments().getSerializable(Constant.COMPANY_MODEL);

        url += company.getId();

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jobsList = response.getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (job.getString("status").equals("true")) {
                                    listJobCompany.add(new Job(
                                            job.getString("_id"),
                                            job.getString("name"),
                                            job.getJSONObject("idCompany").getString("_id"),
                                            job.getJSONObject("idCompany").getString("name"),
                                            job.getString("locationWorking"),
                                            job.getString("salary"),
                                            Constant.setTime(job.getString("deadline")),
                                            job.getString("description"),
                                            job.getString("requirement"),
                                            job.getJSONObject("idOccupation").getString("_id"),
                                            job.getJSONObject("idOccupation").getString("name"),
                                            job.getJSONObject("idCompany").getString("image"),
                                            job.getString("amount"),
                                            job.getString("workingForm"),
                                            job.getString("experience"),
                                            job.getString("gender")
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
                        if (error instanceof com.android.volley.NoConnectionError) {

                        }
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
                throw new VolleyError(error.getMessage());
            }
        });
        queue.add(data);
    }

    private void clickLinkCompany(){
        binding.textLink.setOnClickListener(v->{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(binding.textLink.getText().toString()));
            startActivity(i);
        });
    }

    @Override
    public void onClick(Job job, int position) {
        Intent intent = new Intent(getActivity(), JobDetailActivity.class);
        intent.putExtra("id", job.getId());
        getActivity().finish();
        startActivity(intent);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding, int position) {

    }
}
