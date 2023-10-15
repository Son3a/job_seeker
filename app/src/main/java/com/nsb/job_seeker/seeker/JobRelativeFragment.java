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
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import java.util.List;

public class JobRelativeFragment extends Fragment implements JobListener {
    private View view;
    private JobAdapter jobAdapter;
    private List<Job> listRelatedJob;
    private RecyclerView rcvRelatedJob;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_job_relative, container, false);

        init();
        setEvent();

        return view;
    }

    private void init(){
        rcvRelatedJob = view.findViewById(R.id.rcvRelatedJob);
        listRelatedJob = getArguments().getParcelableArrayList(Program.LIST_RELATED_JOB);
        jobAdapter = new JobAdapter(listRelatedJob, this, true);
        rcvRelatedJob.setAdapter(jobAdapter);
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
