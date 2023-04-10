package com.nsb.job_seeker.employer;

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

public class RecruitmentsFragment extends Fragment {
    private RecruitmentsListViewAdapter recruitmentsListViewAdapter;
    private ListView listViewRecruitments;
    private List<Recruitment> recruitmentList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_employer_recruitments, container, false);
        listViewRecruitments = homeView.findViewById(R.id.lv_recruitments);

        setControl();
        setEvent();
        return homeView;
    }

    private void setControl() {


        recruitmentList = new ArrayList<Recruitment>();
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer","Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5","còn 4 ngày"));
    }



    private void setEvent() {
        recruitmentsListViewAdapter = new RecruitmentsListViewAdapter(getActivity(), R.layout.list_view_item_recruitment, recruitmentList);
        listViewRecruitments.setAdapter(recruitmentsListViewAdapter);
        listViewRecruitments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
}
