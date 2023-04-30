package com.nsb.job_seeker.seeder;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.model.Job;

import java.util.ArrayList;
import java.util.List;

public class ListJobByCompany extends AppCompatActivity {
    private ListView listView;
    private ProgressBar pbLoading;
    private List<Job> jobList;
    private ImageView icBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_list_job_by_company);

        setControl();
        setEvent();
    }

    private void setControl() {
        listView = findViewById(R.id.lv_job_by_company);
        pbLoading = findViewById(R.id.idLoadingPB);
        icBack = findViewById(R.id.ic_back);
        jobList = new ArrayList<>();
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));

    }

    private void setEvent() {
        back();
        setListView();
    }

    private void back() {
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setListView() {
        ListViewApdapter listViewApdapter = new ListViewApdapter(ListJobByCompany.this, R.layout.list_view_item_job, jobList);
        listView.setAdapter(listViewApdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                Intent i = new Intent(ListJobByCompany.this, JobDetailActivity.class);
//                i.putExtra("id", jobList.get(position).getId());
//                i.putExtra("isApply", true);
//                startActivity(i);
            }
        });
    }
}
