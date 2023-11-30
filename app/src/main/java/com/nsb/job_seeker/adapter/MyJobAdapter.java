package com.nsb.job_seeker.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nsb.job_seeker.fragment.seeker.ExtraNewsFragment;
import com.nsb.job_seeker.fragment.seeker.ForMeFragment;
import com.nsb.job_seeker.fragment.seeker.MyJobAppliedFragment;
import com.nsb.job_seeker.fragment.seeker.MyJobSavedFragment;

public class MyJobAdapter extends FragmentStateAdapter {
    public MyJobAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MyJobSavedFragment();
            case 1:
                return new MyJobAppliedFragment();
            default:
                return new MyJobSavedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
