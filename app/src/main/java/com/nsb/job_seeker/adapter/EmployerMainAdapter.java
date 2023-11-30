package com.nsb.job_seeker.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nsb.job_seeker.activity.messenger.MessageFragment;
import com.nsb.job_seeker.fragment.AccountFragment;
import com.nsb.job_seeker.fragment.admin.CompanyFragment;
import com.nsb.job_seeker.fragment.admin.CreateRecruitmentFragment;
import com.nsb.job_seeker.fragment.admin.RecruitmentsFragment;

public class EmployerMainAdapter extends FragmentStateAdapter {
    public EmployerMainAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RecruitmentsFragment();
            case 1:
                return new CreateRecruitmentFragment();
            case 2:
                return new CompanyFragment();
            case 3:
                return new MessageFragment();
            case 4:
                return new AccountFragment();
            default:
                return new RecruitmentsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
