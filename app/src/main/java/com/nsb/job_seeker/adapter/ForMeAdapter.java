package com.nsb.job_seeker.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nsb.job_seeker.seeder.ExtraNewsFragment;
import com.nsb.job_seeker.seeder.ForMeFragment;

public class ForMeAdapter extends FragmentStateAdapter {

    public ForMeAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new ForMeFragment();
            case 1:
                return new ExtraNewsFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
