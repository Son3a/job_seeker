package com.nsb.job_seeker.seeder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.R;

public class MyJobSavedFragment extends Fragment {
    private MyJobSavedFragment myJobSavedFragment;
    private View savedJobView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        savedJobView = inflater.inflate(R.layout.fragment_seeker_saved_job, container, false);

        setControl();
        setEvent();
        return savedJobView;
    }

    private void setControl(){

    }

    private void setEvent(){

    }
}
