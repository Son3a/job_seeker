package com.nsb.job_seeker.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.model.Item;

import java.util.List;

public class FeedAdapter extends ArrayAdapter {

    private Context context;
    private List<Item> listItems;
    private int layoutId;

    public FeedAdapter(@NonNull Context context, int layoutId, List<Item> listItems) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutId, parent, false);

        FeedHolder feedHolder = new FeedHolder();
        feedHolder.item = listItems.get(position);
        feedHolder.tvContent = row.findViewById(R.id.content);
        feedHolder.tvDate = row.findViewById(R.id.date);
        feedHolder.tvTitle = row.findViewById(R.id.title);


        row.setTag(feedHolder);
        String description = feedHolder.item.getContent();

        feedHolder.tvTitle.setText(Html.fromHtml(feedHolder.item.getTitle(), Html.FROM_HTML_MODE_LEGACY).toString());
        feedHolder.tvDate.setText(Html.fromHtml(feedHolder.item.getPubDate(), Html.FROM_HTML_MODE_LEGACY).toString());
        feedHolder.tvContent.setText(Html.fromHtml(description.substring(0, description.indexOf("<br>")), Html.FROM_HTML_MODE_LEGACY).toString());

        return row;
    }

    private void setHeightLineItem(View view){
        RelativeLayout layoutContent = view.findViewById(R.id.layout_content);
        int height = layoutContent.getMeasuredHeight();
        View line = view.findViewById(R.id.line);
        ViewGroup.LayoutParams params = line.getLayoutParams();
        params.width = 2;
        params.height = height;
        line.setLayoutParams(params);

    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    public static class FeedHolder {
        Item item;
        TextView tvTitle, tvDate, tvContent;
    }

}
