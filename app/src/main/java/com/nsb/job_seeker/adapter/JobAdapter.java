package com.nsb.job_seeker.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobHolder> {

    private boolean isVisibleBtnSave;
    private boolean isSaveView = false;
    private final List<Job> jobList;
    private final JobListener listener;

    public JobAdapter(List<Job> jobList, JobListener listener, boolean isVisibleBtnSave) {
        this.isVisibleBtnSave = isVisibleBtnSave;
        this.jobList = jobList;
        this.listener = listener;
    }

    public JobAdapter(List<Job> jobList, JobListener listener, boolean isVisibleBtnSave, boolean isSaveView) {
        this.isVisibleBtnSave = isVisibleBtnSave;
        this.jobList = jobList;
        this.listener = listener;
        this.isSaveView = isSaveView;
    }

    @NonNull
    @Override
    public JobHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListViewItemJobBinding listViewItemJobBinding = ListViewItemJobBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new JobHolder(listViewItemJobBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull JobHolder holder, int position) {
        holder.setData(jobList.get(position));

        holder.binding.imgSaveJob.setOnClickListener(v -> {
            listener.onSave(jobList.get(position), position, isSaveView, holder.binding);
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }


    class JobHolder extends RecyclerView.ViewHolder {
        ListViewItemJobBinding binding;

        public JobHolder(@NonNull ListViewItemJobBinding listViewItemJobBinding) {
            super(listViewItemJobBinding.getRoot());
            this.binding = listViewItemJobBinding;
        }

        void setData(Job job) {
            binding.tvNameJob.setText(job.getNameJob());
            binding.tvCompany.setText(job.getCompany());
            binding.tvPlace.setText(job.getPlace());
            binding.tvSalary.setText(job.getSalary());
            binding.tvTimeUpdated.setText(job.getTime_update());

            if (isVisibleBtnSave == false) { //hide icon
                binding.imgSaveJob.setVisibility(View.GONE);
            }

            if (isSaveView) {
                binding.imgSaveJob.setImageResource(R.drawable.ic_save_job1);
                binding.imgSaveJob.setTag("save");
            } else {
                if (Program.idSavedJobs.contains(job.getId())) {
                    binding.imgSaveJob.setImageResource(R.drawable.ic_save_job1);
                    binding.imgSaveJob.setTag("save");
                } else {
                    binding.imgSaveJob.setImageResource(R.drawable.ic_save_job);
                    binding.imgSaveJob.setTag("not save");
                }
            }

            binding.getRoot().setOnClickListener(v -> {
                listener.onClick(job);
            });
        }
    }


}