package com.nsb.job_seeker.seeder;


import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.auth.DialogNotification;
import com.nsb.job_seeker.auth.RegisterActivity;
import com.nsb.job_seeker.employer.CVListViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewApdapter extends ArrayAdapter {

    private Context context;
    private int layoutId;
    private List<Job> jobList;
    private RequestQueue mRequestQueue;
    private String base_url = Program.url_dev + "/job";
    private DialogNotification dialogNotification = null;
    private ProgressBar pbLoading;
    private ImageView btnSaveJob;

    private boolean isVisibleBtnSave;
    private boolean isSaveView = false;

    public ListViewApdapter(@NonNull Context context, int layoutId, List<Job> jobList, boolean isVisibleBtnSave) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.jobList = jobList;
        this.isVisibleBtnSave = isVisibleBtnSave;
    }

    public ListViewApdapter(@NonNull Context context, int layoutId, List<Job> jobList, boolean isVisibleBtnSave, boolean isSaveView) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.jobList = jobList;
        this.isVisibleBtnSave = isVisibleBtnSave;
        this.isSaveView = isSaveView;
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
        holder.btnSave = row.findViewById(R.id.img_save_job);

        row.setTag(holder);
        holder.tvNameJob.setText(holder.job.getNameJob());
        holder.tvCompany.setText(holder.job.getCompany());
        holder.tvPlace.setText(holder.job.getPlace());
        holder.tvSalary.setText(holder.job.getSalary());
        holder.tvTimeUpdated.setText(holder.job.getTime_update());

        pbLoading = ((Activity) context).findViewById(R.id.idLoadingPB);

        hideBtnSave(row, position);

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    toggleJobFavourite(jobList.get(position).getId(), position, holder.btnSave);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return row;
    }

    private void hideBtnSave(View row, int position) {
        ImageView imgSaveButton = (ImageView) row.findViewById(R.id.img_save_job);
        if (isVisibleBtnSave == false) {
            imgSaveButton.setVisibility(View.GONE);
        }
    }

    private void toggleJobFavourite(String jobId, int position, ImageView imgSave) throws JSONException {
        mRequestQueue = Volley.newRequestQueue(getContext());
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
        pbLoading.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    dialogNotification.openDialogNotification(message.substring( 0, message.length() - 1 ), getContext());
                    pbLoading.setVisibility(View.GONE);
                    if (isSaveView) {
                        jobList.remove(position);
                        notifyDataSetChanged();
                    }else{
                        imgSave.setImageResource(R.drawable.ic_save_job1);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    pbLoading.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        dialogNotification.openDialogNotification(message.substring(1, message.length() - 1), getContext());
                        pbLoading.setVisibility(View.GONE);
                        Log.d("ABC", body);
                    } catch (UnsupportedEncodingException e) {
                        pbLoading.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(Program.sharedPreferencesName, MODE_PRIVATE);
                String ACCESSTOKEN = sharedPreferences.getString("accessToken", "");
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer " + ACCESSTOKEN.substring(1, ACCESSTOKEN.length() - 1));
                return params;
            }

            ;
        };
        mRequestQueue.add(jsonObjectRequest);
    }

    public static class JobHolder {
        Job job;
        TextView tvCompany, tvNameJob, tvPlace, tvSalary, tvTimeUpdated;
        ImageView btnSave;
    }

}
