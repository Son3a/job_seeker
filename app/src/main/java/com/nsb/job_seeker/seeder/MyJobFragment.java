package com.nsb.job_seeker.seeder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.nsb.job_seeker.R;

public class MyJobFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private View myJobView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myJobView = inflater.inflate(R.layout.fragment_seeker_my_fob, container, false);

        setControl();
        setEvent();
        return myJobView;
    }

    private void setControl() {
        tabLayout = myJobView.findViewById(R.id.tab_layout);
        viewPager2 = myJobView.findViewById(R.id.view_pager2);

        tabLayout.addTab(tabLayout.newTab().setText("Saved Job"));
        tabLayout.addTab(tabLayout.newTab().setText("Applied Job"));
    }

    private void setEvent() {
        viewPager2.setAdapter(new FragmentStateAdapter(this) {
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
                return tabLayout.getTabCount();
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
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
