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

import java.util.List;

public class CVListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutId;
    private List<String> timeAppliedList;

    public CVListViewAdapter(@NonNull Context context, int layoutId, List<String> timeAppliedList) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.timeAppliedList = timeAppliedList;
    }

    @Override
    public int getCount() {
        return timeAppliedList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutId, parent, false);

        CVListViewAdapter.Holder holder = new CVListViewAdapter.Holder();
        holder.timeApplied = timeAppliedList.get(position);
        holder.tvTimeApplied = (TextView) row.findViewById(R.id.tv_time_applied);
        holder.tvSTT = (TextView) row.findViewById(R.id.tv_stt);
        holder.tvFile = (TextView) row.findViewById(R.id.tv_file);

        row.setTag(holder);
        holder.tvTimeApplied.setText(holder.timeApplied);
        holder.tvSTT.setText(String.valueOf(position + 1));
        holder.tvFile.setText("Xem ngay");

        return row;
    }

    public static class Holder {
        String timeApplied;
        TextView tvTimeApplied, tvSTT, tvFile;
    }
}
