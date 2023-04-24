package com.nsb.job_seeker.employer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nsb.job_seeker.R;

import java.util.ArrayList;

public class ListCVActivity extends AppCompatActivity {
    private CVListViewAdapter cvListViewAdapter;
    private ListView listViewCV;
    private ArrayList<String> CVList;
    private ImageView icBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_employer_list_cv);

        setControl();
        setEvent();
    }

    private void setControl(){
        icBack = findViewById(R.id.ic_back);

        listViewCV = findViewById(R.id.lv_cv);

        CVList = new ArrayList<String>();
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
        CVList.add("12/02/2023");
    }

    private void setEvent(){
        cvListViewAdapter = new CVListViewAdapter(ListCVActivity.this,R.layout.list_view_item_cv,CVList);
        listViewCV.setAdapter(cvListViewAdapter);
        listViewCV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}
