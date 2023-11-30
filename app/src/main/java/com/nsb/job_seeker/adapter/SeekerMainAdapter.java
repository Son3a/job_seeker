package com.nsb.job_seeker.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nsb.job_seeker.activity.messenger.MessageFragment;
import com.nsb.job_seeker.fragment.AccountFragment;
import com.nsb.job_seeker.fragment.seeker.MyJobFragment;
import com.nsb.job_seeker.fragment.seeker.NewsJobFragment;

public class SeekerMainAdapter extends FragmentStateAdapter {
    public SeekerMainAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new NewsJobFragment();
            case 1:
                return new MyJobFragment();
            case 2:
                return new MessageFragment();
            case 3:
                return new AccountFragment();
            default:
                return new NewsJobFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
