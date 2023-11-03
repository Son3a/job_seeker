package com.nsb.job_seeker.fragment.seeker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.ForMeAdapter;

public class NewsJobFragment extends Fragment {
    private View newsJobView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ForMeAdapter forMeAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        newsJobView = inflater.inflate(R.layout.fragment_seeker_news_job, container, false);

        setControl();
        setEvent();

        return newsJobView;
    }

    private void setControl() {
        tabLayout = newsJobView.findViewById(R.id.tab_layout);
        viewPager2 = newsJobView.findViewById(R.id.view_pager);
        if (getActivity() != null) {
            forMeAdapter = new ForMeAdapter(getActivity());
        }
    }

    private void setEvent() {
        viewPager2.setAdapter(forMeAdapter);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            tab.setIcon(R.drawable.ic_circle);
            tab.view.setClickable(false);
        }).attach();
    }
}
