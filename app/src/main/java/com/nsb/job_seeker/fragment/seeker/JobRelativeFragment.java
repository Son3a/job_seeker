package com.nsb.job_seeker.fragment.seeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.databinding.FragmentJobRelativeBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import java.util.List;

public class JobRelativeFragment extends Fragment implements JobListener {
    private FragmentJobRelativeBinding binding;
    private JobAdapter jobAdapter;
    private List<Job> listRelatedJob;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentJobRelativeBinding.inflate(getLayoutInflater());

        init();
        setEvent();

        return binding.getRoot();
    }

    private void init() {
        listRelatedJob = getArguments().getParcelableArrayList(Constant.LIST_RELATED_JOB);
        jobAdapter = new JobAdapter(getContext(), listRelatedJob, this, true);
        binding.rcvRelatedJob.setAdapter(jobAdapter);
    }

    private void setEvent() {

    }

    @Override
    public void onClick(Job job, int position) {
        Intent i = new Intent(getContext(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        startActivity(i);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding, int position) {

    }
}
