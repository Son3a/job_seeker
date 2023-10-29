package com.nsb.job_seeker.seeker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
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

    private void init(){
        listRelatedJob = getArguments().getParcelableArrayList(Program.LIST_RELATED_JOB);
        jobAdapter = new JobAdapter(listRelatedJob, this, true);
        binding.rcvRelatedJob.setAdapter(jobAdapter);
    }

    private void setEvent(){

    }

    @Override
    public void onClick(Job job) {

    }

    @Override
    public void onSave(Job job, int position, boolean isSaveView, ListViewItemJobBinding binding) {

    }
}
