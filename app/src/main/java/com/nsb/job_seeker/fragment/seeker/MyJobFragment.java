package com.nsb.job_seeker.fragment.seeker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.databinding.FragmentSeekerForMeBinding;
import com.nsb.job_seeker.databinding.FragmentSeekerMyFobBinding;
import com.nsb.job_seeker.model.Job;

import java.util.ArrayList;
import java.util.List;

public class MyJobFragment extends Fragment {
    private FragmentSeekerMyFobBinding binding;
    //    private ViewPager2 viewPager2;
    private MyJobAppliedFragment myJobAppliedFragment;
    private MyJobSavedFragment myJobSavedFragment;

    private List<Job> jobListApplied;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeekerMyFobBinding.inflate(getLayoutInflater());

        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã lưu lại"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã nộp hồ sơ"));

        myJobSavedFragment = new MyJobSavedFragment();
        myJobAppliedFragment = new MyJobAppliedFragment();
        jobListApplied = new ArrayList<>();
    }

    private void setEvent() {
        setTabLayout();
    }

    private void setTabLayout() {
        getParentFragmentManager().beginTransaction().replace(R.id.container_my_job, myJobSavedFragment).commit();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    getParentFragmentManager().beginTransaction().replace(R.id.container_my_job, myJobSavedFragment).commit();
                } else {
                    getParentFragmentManager().beginTransaction().replace(R.id.container_my_job, myJobAppliedFragment).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


}
