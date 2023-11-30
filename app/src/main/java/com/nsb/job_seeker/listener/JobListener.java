package com.nsb.job_seeker.listener;

import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.model.Job;

public interface JobListener {
    void onClick(Job job, int position);

    void onSave(Job job, ListViewItemJobBinding binding, int position);
}
