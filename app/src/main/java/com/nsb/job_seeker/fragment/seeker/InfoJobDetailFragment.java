package com.nsb.job_seeker.fragment.seeker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.JOB_MODEL.equals(intent.getAction())) {
                Job job1 = (Job) intent.getSerializableExtra(Constant.JOB_MODEL);
                binding.textExperience.setText(job1.getExperience());
                binding.textWorkingForm.setText(job1.getWorkingForm());
                binding.textAmount.setText(job1.getAmountRecruitment() + " người");
                binding.textGender.setText(job1.getGender());
                binding.textRequire.setText(Constant.formatStringToBullet(job1.getReqJob()));
                binding.textDescription.setText(Constant.formatStringToBullet(job1.getDesJob()));
            }
        }
    };
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

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Constant.JOB_MODEL);
        getContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(broadcastReceiver);
    }
}
