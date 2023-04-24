package com.nsb.job_seeker.seeder;

import static android.content.Intent.getIntent;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyJobFragment extends Fragment {

    private TabLayout tabLayout;
    //    private ViewPager2 viewPager2;
    private View myJobView;
    private MyJobAppliedFragment myJobAppliedFragment;
    private MyJobSavedFragment myJobSavedFragment;
    private ProgressBar pbLoading;

    private List<Job> jobListApplied;

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
//        viewPager2 = myJobView.findViewById(R.id.view_pager2);

        tabLayout.addTab(tabLayout.newTab().setText("Đã lưu lại"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã nộp hồ sơ"));

        myJobSavedFragment = new MyJobSavedFragment();
        myJobAppliedFragment = new MyJobAppliedFragment();

        pbLoading = myJobView.findViewById(R.id.idLoadingPB);
        jobListApplied = new ArrayList<>();
    }

    private void setEvent() {
         setTabLayout();
    }

    private void setTabLayout() {
        getParentFragmentManager().beginTransaction().replace(R.id.container_my_job, myJobSavedFragment).commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
