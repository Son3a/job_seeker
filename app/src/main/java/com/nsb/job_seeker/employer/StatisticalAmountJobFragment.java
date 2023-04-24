package com.nsb.job_seeker.employer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nsb.job_seeker.R;

import java.util.ArrayList;
import java.util.Arrays;

public class StatisticalAmountJobFragment extends Fragment {
    private Spinner spnJob;
    private View statisticalAmountJobView;
    private ProgressBar pbLoading;
    private ArrayList<String> locationList = new ArrayList<>(Arrays.asList("Tất cả", "Hồ Chí Minh", "Hà Nội", "Đà Nẵng"));


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        statisticalAmountJobView = inflater.inflate(R.layout.fragment_employer_statistical_amount_job, container, false);

        setControl();
        setEvent();

        return statisticalAmountJobView;
    }

    private void setControl() {
        pbLoading = statisticalAmountJobView.findViewById(R.id.idLoadingPB);
        spnJob = statisticalAmountJobView.findViewById(R.id.spinner_job);
    }

    private void setEvent() {
        bindingDataToSpinner();
    }

    private void bindingDataToSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, locationList);
        spnJob.setAdapter(adapter);
    }
}
