package com.nsb.job_seeker.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobHolder> {

    private boolean isVisibleBtnSave;
    private final List<Job> jobList;
    private final JobListener listener;
    private final Context context;

    public JobAdapter(Context context, List<Job> jobList, JobListener listener, boolean isVisibleBtnSave) {
        this.context = context;
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
            listener.onSave(jobList.get(position), holder.binding, position);
        });

        holder.binding.getRoot().setOnClickListener(v -> {
            listener.onClick(jobList.get(position), position);
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
            binding.tvCompany.setText(job.getCompanyName());
            binding.tvPlace.setText(job.getPlace());
            binding.tvSalary.setText(job.getSalary());
            if (job.getImage() != null && !job.getImage().equals("")) {
                if(!URLUtil.isValidUrl(job.getImage())) {
                    binding.imgJob.setImageBitmap(Constant.getBitmapFromEncodedString(job.getImage()));
                } else {
                    Picasso.get().load(job.getImage()).into(binding.imgJob);
                }
            }

            if (isVisibleBtnSave == false) { //hide icon
                binding.imgSaveJob.setVisibility(View.GONE);
            }
            if (Constant.idSavedJobs != null) {
                if (Constant.idSavedJobs.contains(job.getId())) {
                    binding.imgSaveJob.setImageResource(R.drawable.ic_saved);
                    binding.imgSaveJob.setColorFilter(ContextCompat.getColor(context, R.color.green));
                } else {
                    binding.imgSaveJob.setImageResource(R.drawable.ic_not_save);
                    binding.imgSaveJob.setColorFilter(ContextCompat.getColor(context, R.color.secondary_text));
                }
            }
            try {
                if (Constant.setTime(job.getDeadline()) == null) {
                    binding.tvTimeUpdated1.setText("Công việc đã hết hạn");
                    binding.textDeadline.setVisibility(View.INVISIBLE);
                    binding.tvTimeUpdated2.setVisibility(View.INVISIBLE);
                } else {
                    binding.textDeadline.setText(Constant.setTime(job.getDeadline()));
                }
            } catch (ParseException e) {
                Log.d("Error", "Err format date: "+ e.getMessage());
            }
        }
    }


}