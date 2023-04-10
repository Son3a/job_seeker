package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListViewApdapter listViewApdapter;
    private ListView listViewJob;
    private List<Job> jobList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_seeker_home, container, false);
        listViewJob = homeView.findViewById(R.id.lv_job);

        setControl();
        setEvent();
        return homeView;
    }

    private void setControl() {


        jobList = new ArrayList<Job>();
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
        jobList.add(new Job("Graphic Designer (Chuyên Viên Thiết Kế Đồ Họa)",
                "Xgeme Studio", "Hà Nội, Việt Nam", "VND 7.000.000 - 12.000.000", "Cập nhật 37 phút trước"));
    }


    private void setEvent() {
        listViewApdapter = new ListViewApdapter(getActivity(), R.layout.list_view_item_job, jobList);
        listViewJob.setAdapter(listViewApdapter);
        listViewJob.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(),JobDetailActivity.class);
                startActivity(i);
            }
        });
    }
}
