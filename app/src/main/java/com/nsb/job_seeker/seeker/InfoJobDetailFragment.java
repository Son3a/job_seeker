package com.nsb.job_seeker.seeker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.nsb.job_seeker.model.Job;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class InfoJobDetailFragment extends Fragment {
    private View view;
    private TextView tvDescJob, tvReq, tvTypeJob;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info_job_detail, container, false);
        init();
        setEvent();

        return view;
    }

    private void init() {
        tvTypeJob = view.findViewById(R.id.textTypeJob);
        tvDescJob = view.findViewById(R.id.textDescription);
        tvReq = view.findViewById(R.id.textRequire);
    }

    private void setEvent() {
        loadInfo();
    }

    private void loadInfo() {
        Job job = (Job) getArguments().getSerializable(Program.JOB_MODEL);

        tvReq.setText(job.getReqJob());
        tvDescJob.setText(job.getDesJob());
        tvTypeJob.setText(job.getTypeJob());
    }
}
