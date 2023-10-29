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
import com.nsb.job_seeker.databinding.FragmentInfoJobDetailBinding;
import com.nsb.job_seeker.model.Job;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class InfoJobDetailFragment extends Fragment {
    private FragmentInfoJobDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInfoJobDetailBinding.inflate(getLayoutInflater());

        init();
        setEvent();

        return binding.getRoot();
    }

    private void init() {

    }

    private void setEvent() {
        loadInfo();
    }

    private void loadInfo() {
        Job job = (Job) getArguments().getSerializable(Program.JOB_MODEL);

        binding.textExperience.setText(job.getExperience());
        binding.textWorkingForm.setText(job.getWorkingForm());
        binding.textAmount.setText(job.getAmountRecruitment() + " người");
        binding.textGender.setText(job.getGender());
        binding.textRequire.setText(Program.formatStringToBullet(job.getReqJob()));
        binding.textDescription.setText(Program.formatStringToBullet(job.getDesJob()));
    }
}
