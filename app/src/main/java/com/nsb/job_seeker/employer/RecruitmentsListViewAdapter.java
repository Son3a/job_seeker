package com.nsb.job_seeker.employer;

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
import com.nsb.job_seeker.seeder.Job;
import com.nsb.job_seeker.seeder.ListViewApdapter;

import java.util.List;

public class RecruitmentsListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutId;
    private List<Recruitment> recruitmentList;

    public RecruitmentsListViewAdapter(@NonNull Context context, int layoutId, List<Recruitment> recruitmentList) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.recruitmentList = recruitmentList;
    }

    @Override
    public int getCount() {
        return recruitmentList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutId, parent,false);

        RecruitmentsListViewAdapter.JobHolder holder = new RecruitmentsListViewAdapter.JobHolder();
        holder.recruitment = recruitmentList.get(position);
        holder.tvTimeCreated = (TextView) row.findViewById(R.id.tv_time_created);
        holder.tvNameJob = (TextView) row.findViewById(R.id.tv_name_job);
        holder.tvPlace = (TextView) row.findViewById(R.id.tv_place);
        holder.tvDeadline = (TextView) row.findViewById(R.id.tv_deadline);
        holder.tvAmountCV = (TextView) row.findViewById(R.id.tv_amount_cv);

        row.setTag(holder);
        holder.tvNameJob.setText(holder.recruitment.getNameJob());
        holder.tvTimeCreated.setText(holder.recruitment.getTimeCreated());
        holder.tvPlace.setText(holder.recruitment.getPlace());
        holder.tvDeadline.setText(holder.recruitment.getDeadline());
        holder.tvAmountCV.setText(holder.recruitment.getAmountCV());

        return row;
    }

    public static class JobHolder{
        Recruitment recruitment;
        TextView tvNameJob,tvPlace,tvTimeCreated,tvDeadline,tvAmountCV;
    }
}
