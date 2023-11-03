package com.nsb.job_seeker.adapter;

import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.model.Company;
import com.nsb.job_seeker.model.Job;
import com.nsb.job_seeker.fragment.seeker.CompanyFragment;
import com.nsb.job_seeker.fragment.seeker.InfoJobDetailFragment;
import com.nsb.job_seeker.fragment.seeker.JobRelativeFragment;

import java.util.ArrayList;
import java.util.List;

public class JobDetailAdapter extends FragmentStateAdapter {

    private Job job;
    private List<Job> listRelatedJob;
    private Company company;
    private InfoJobDetailFragment infoJobDetailFragment = new InfoJobDetailFragment();
    private JobRelativeFragment jobRelativeFragment = new JobRelativeFragment();
    private CompanyFragment companyFragment = new CompanyFragment();

    public JobDetailAdapter(@NonNull FragmentActivity fragmentActivity, List<Job> listRelatedJob, Job job, Company company) {
        super(fragmentActivity);
        this.job = job;
        this.company = company;
        this.listRelatedJob = listRelatedJob;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle;
        switch (position) {
            case 0:
                bundle = new Bundle();
                bundle.putSerializable(Constant.JOB_MODEL, job);
                infoJobDetailFragment.setArguments(bundle);
                return infoJobDetailFragment;
            case 1:
                bundle = new Bundle();
                bundle.putParcelableArrayList(Constant.LIST_RELATED_JOB, (ArrayList<? extends Parcelable>) listRelatedJob);
                jobRelativeFragment.setArguments(bundle);
                return jobRelativeFragment;
            case 2:
                bundle = new Bundle();
                bundle.putSerializable(Constant.COMPANY_MODEL, company);
                companyFragment.setArguments(bundle);
                return companyFragment;
            default:
                return infoJobDetailFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
