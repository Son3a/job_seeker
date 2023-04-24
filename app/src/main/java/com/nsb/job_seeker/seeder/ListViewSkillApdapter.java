package com.nsb.job_seeker.seeder;

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

import java.util.ArrayList;

public class ListViewSkillApdapter extends ArrayAdapter {
    private Context context;
    private int layoutId;
    private String[] skillList;

    public ListViewSkillApdapter(@NonNull Context context, int layoutId, String[] skillList) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.skillList = skillList;
    }

    @Override
    public int getCount() {
        return skillList.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutId, parent,false);

        TextView tvSkill = row.findViewById(R.id.tv_item_skill);
        tvSkill.setText(skillList[position]);
        return row;
    }
}
