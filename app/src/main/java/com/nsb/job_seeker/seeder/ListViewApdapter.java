package com.nsb.job_seeker.seeder;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.model.Job;

import java.util.List;

public class ListViewApdapter extends ArrayAdapter {

    private Context context;
    private int layoutId;
    private List<Job> jobList;
    private boolean isVisibleBtnSave;

    public ListViewApdapter(@NonNull Context context, int layoutId, List<Job> jobList, boolean isVisibleBtnSave) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.jobList = jobList;
        this.isVisibleBtnSave = isVisibleBtnSave;
    }

    @Override
    public int getCount() {
        return jobList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutId, parent, false);

        JobHolder holder = new JobHolder();
        holder.job = jobList.get(position);
        holder.tvCompany = (TextView) row.findViewById(R.id.tv_company);
        holder.tvNameJob = (TextView) row.findViewById(R.id.tv_name_job);
        holder.tvPlace = (TextView) row.findViewById(R.id.tv_place);
        holder.tvSalary = (TextView) row.findViewById(R.id.tv_salary);
        holder.tvTimeUpdated = (TextView) row.findViewById(R.id.tv_time_updated);

        row.setTag(holder);
        holder.tvNameJob.setText(holder.job.getNameJob());
        holder.tvCompany.setText(holder.job.getCompany());
        holder.tvPlace.setText(holder.job.getPlace());
        holder.tvSalary.setText(holder.job.getSalary());
        holder.tvTimeUpdated.setText(holder.job.getTime_update());

        hideBtnSave(row,position);

        saveJob(row,position);

        return row;
    }

    private void hideBtnSave(View row, int position){
        ImageView imgSaveButton = (ImageView) row.findViewById(R.id.img_save_job);
        if(isVisibleBtnSave == false){
            imgSaveButton.setVisibility(View.GONE);
        }
    }

    private void saveJob(View row, int position){
        ImageView imgSaveButton = (ImageView) row.findViewById(R.id.img_save_job);
        boolean check = true;
        imgSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("CLick on job " + position);
//                if (check == true) {
//                    imgSaveButton.setImageResource(R.drawable.ic_save_job1);
//                    check = false;
//                }
            }
        });
    }

    public static class JobHolder {
        Job job;
        TextView tvCompany, tvNameJob, tvPlace, tvSalary, tvTimeUpdated;
    }

}
