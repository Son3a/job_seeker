package com.nsb.job_seeker.fragment.seeker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.ForMeAdapter;
import com.nsb.job_seeker.adapter.MyJobAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.FragmentSeekerForMeBinding;
import com.nsb.job_seeker.databinding.FragmentSeekerMyFobBinding;
import com.nsb.job_seeker.model.Job;

import java.util.ArrayList;
import java.util.List;

public class MyJobFragment extends Fragment {
    private FragmentSeekerMyFobBinding binding;
    private MyJobAdapter myJobAdapter;
    private List<String> tabTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeekerMyFobBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
        tabTitle = new ArrayList<>();
        tabTitle.add("Đã lưu lại");
        tabTitle.add("Đã nộp hồ sơ");
        if (getActivity() != null) {
            myJobAdapter = new MyJobAdapter(getActivity());
        }
    }

    private void setEvent() {
        binding.viewPagerMyJob.setAdapter(myJobAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPagerMyJob, (tab, position) -> {
            tab.setText(tabTitle.get(position));
            tab.view.setClickable(true);
        }).attach();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MyJob", "is pause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MyJob", "is resume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MyJob", "is start");
    }
}
