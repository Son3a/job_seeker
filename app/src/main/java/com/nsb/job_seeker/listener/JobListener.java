package com.nsb.job_seeker.listener;

import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.model.Job;

public interface JobListener {
    void onClick(Job job);

    void onSave(Job job, int position, ListViewItemJobBinding binding);
}
