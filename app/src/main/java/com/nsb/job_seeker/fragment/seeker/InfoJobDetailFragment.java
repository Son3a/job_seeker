package com.nsb.job_seeker.fragment.seeker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.FragmentInfoJobDetailBinding;
import com.nsb.job_seeker.model.Job;

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
        Job job = (Job) getArguments().getSerializable(Constant.JOB_MODEL);

        binding.textExperience.setText(job.getExperience());
        binding.textWorkingForm.setText(job.getWorkingForm());
        binding.textAmount.setText(job.getAmountRecruitment() + " người");
        binding.textGender.setText(job.getGender());
        binding.textRequire.setText(Constant.formatStringToBullet(job.getReqJob()));
        binding.textDescription.setText(Constant.formatStringToBullet(job.getDesJob()));
    }
}
