package com.nsb.job_seeker.seeder;

import static java.lang.String.format;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nsb.job_seeker.R;

import java.io.PrintStream;

public class StatisticalPieChartAdapter extends ArrayAdapter{

    private String[] listNameJobs;
    private int[] listAmountJobs;
    private Context context;
    private int layoutId, sum;
    public StatisticalPieChartAdapter(@NonNull Context context, int layoutId, String[] listNameJobs, int[] listAmountJobs, int sum) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.listNameJobs = listNameJobs;
        this.listAmountJobs = listAmountJobs;
        this.sum = sum;
        PrintStream out = System.out;
        out.println( sum);
    }
    @Override
    public int getCount() {
        return listAmountJobs.length;
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutId, parent,false);

        StatisticalPieChartAdapter.JobHolder holder = new StatisticalPieChartAdapter.JobHolder();

        holder.nameJob = listNameJobs[position];
        holder.amountJob = listAmountJobs[position];

        holder.txtNameJob = (TextView) row.findViewById(R.id.txt_name_job);
        holder.txtAmountJob = (TextView) row.findViewById(R.id.txt_amount_job);


        row.setTag(holder);
        holder.txtNameJob.setText(holder.nameJob);
        float percent = ((float)(holder.amountJob)/sum)*100;
        format("%.2f", percent);
        String amountJob = String.valueOf(holder.amountJob) + " (" + String.valueOf(percent) + "%)";
        holder.txtAmountJob.setText(amountJob);

        return row;
    }

    public static class JobHolder{
        String nameJob;
        int amountJob;
        TextView txtNameJob, txtAmountJob;
    }
}
