package com.nsb.job_seeker.seeder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nsb.job_seeker.R;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchFragment extends Fragment {

    private ImageView imageView; //test
    private Spinner spinnerLocation, spinnerSalary, spinnerTimeUpdated;
    private Button btnSubmit, btnRest;
    private View searchView, bottomSheetDialogView;
    private BottomSheetDialog bottomSheetDialog;

    private ArrayList<String> locationList = new ArrayList<>(Arrays.asList("Tất cả", "Hồ Chí Minh", "Hà Nội", "Đà Nẵng"));
    private ArrayList<String> salaryList = new ArrayList<>(Arrays.asList("Tất cả", "Dưới $300", "$300 - $500", "$500 - $700", "$700 - $1000", "$1000 - $1200", "$1200 - 1500", "$1500 - $200"));
    private ArrayList<String> timeUpdated = new ArrayList<>(Arrays.asList("Mới nhất", "Cũ nhất"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        searchView = inflater.inflate(R.layout.fragment_seeker_search, container, false);
        bottomSheetDialogView = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        setControl();
        setEvent(inflater, container);

        return searchView;
    }

    private void setControl() {
        spinnerLocation = bottomSheetDialogView.findViewById(R.id.spinner_location);
        spinnerSalary = bottomSheetDialogView.findViewById(R.id.spinner_salary);
        spinnerTimeUpdated = bottomSheetDialogView.findViewById(R.id.spinner_time_updated);
        btnSubmit = bottomSheetDialogView.findViewById(R.id.btn_submit);
        btnRest = bottomSheetDialogView.findViewById(R.id.btn_reset);

        imageView = (ImageView) searchView.findViewById(R.id.img_filter);
    }

    private void setEvent(LayoutInflater inflater, ViewGroup container) {
        setDataFilter();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();
            }
        });

        btnRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerLocation.setSelection(0);
                spinnerTimeUpdated.setSelection(0);
                spinnerSalary.setSelection(0);
            }
        });
    }

    private void bindingDataToSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, locationList);
        spinnerLocation.setAdapter(adapter);

        adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, salaryList);
        spinnerSalary.setAdapter(adapter);

        adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, timeUpdated);
        spinnerTimeUpdated.setAdapter(adapter);
    }

    private void setDataFilter(){
        bindingDataToSpinner();
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(bottomSheetDialogView);
    }
}
