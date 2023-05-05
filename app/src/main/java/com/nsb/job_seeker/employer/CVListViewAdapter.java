package com.nsb.job_seeker.employer;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nsb.job_seeker.R;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class CVListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutId;
    private List<String> timeAppliedList;
    private List<String> listFile;

    public CVListViewAdapter(@NonNull Context context, int layoutId, List<String> timeAppliedList, List<String> listFile) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.timeAppliedList = timeAppliedList;
        this.listFile = listFile;
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

        TextView tvFile = row.findViewById(R.id.tv_file);
        tvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pdf_url = listFile.get(position);
                try {
                    if(isValidURL(pdf_url)) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdf_url));
                        context.startActivity(browserIntent);
                    }
                    else{
                        Toast.makeText(context, "Lỗi! Không thể mở file", Toast.LENGTH_SHORT).show();
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        return row;
    }

    private boolean isValidURL(String url) throws MalformedURLException, URISyntaxException {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static class Holder {
        String timeApplied;
        TextView tvTimeApplied, tvSTT, tvFile;
    }
}
