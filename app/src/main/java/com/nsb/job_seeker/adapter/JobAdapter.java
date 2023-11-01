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
import com.squareup.picasso.Picasso;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobHolder> {

    private boolean isVisibleBtnSave;
    private final List<Job> jobList;
    private final JobListener listener;

    public JobAdapter(List<Job> jobList, JobListener listener, boolean isVisibleBtnSave) {
        this.isVisibleBtnSave = isVisibleBtnSave;
        this.jobList = jobList;
        this.listener = listener;
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
            listener.onSave(jobList.get(position), position, holder.binding);
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
            Picasso.get().load(job.getImage()).into(binding.imgJob);
            if (job.getDeadline() == null) {
                binding.tvTimeUpdated1.setText("Công việc đã hết hạn");
                binding.tvTimeUpdated.setVisibility(View.INVISIBLE);
                binding.tvTimeUpdated2.setVisibility(View.INVISIBLE);
            }
            //binding.tvTimeUpdated.setText(job.getTime_update());

            if (isVisibleBtnSave == false) { //hide icon
                binding.imgSaveJob.setVisibility(View.GONE);
            }
            if (Program.idSavedJobs.contains(job.getId())) {
                binding.imgSaveJob.setImageResource(R.drawable.ic_saved);
            } else {
                binding.imgSaveJob.setImageResource(R.drawable.ic_not_save);
            }


            binding.getRoot().setOnClickListener(v -> {
                listener.onClick(job);
            });
        }
    }


}