package com.nsb.job_seeker.adapter;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.model.Job;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobAdapter extends ArrayAdapter {

    private Context context;
    private int layoutId;
    private List<Job> jobList;
    private RequestQueue mRequestQueue;
    private String base_url = Program.url_dev + "/job";
    private DialogNotification dialogNotification = null;
    private ProgressBar pbLoading;
    private PreferenceManager preferenceManager;
    private List<String> listSavedJob;

    private boolean isVisibleBtnSave;
    private boolean isSaveView = false;

    public JobAdapter(@NonNull Context context, int layoutId, List<Job> jobList, boolean isVisibleBtnSave) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.jobList = jobList;
        this.isVisibleBtnSave = isVisibleBtnSave;
    }

    public JobAdapter(@NonNull Context context, int layoutId, List<Job> jobList, boolean isVisibleBtnSave, boolean isSaveView) {
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
        FrameLayout layoutItemJob = row.findViewById(R.id.layout_item_job);
        preferenceManager = new PreferenceManager(context);
        listSavedJob = preferenceManager.getArray(Program.LIST_SAVED_JOB);

        JobHolder holder = new JobHolder();
        holder.job = jobList.get(position);
        holder.tvCompany = (TextView) row.findViewById(R.id.tv_company);
        holder.tvNameJob = (TextView) row.findViewById(R.id.tv_name_job);
        holder.tvPlace = (TextView) row.findViewById(R.id.tv_place);
        holder.tvSalary = (TextView) row.findViewById(R.id.tv_salary);
        holder.tvTimeUpdated = (TextView) row.findViewById(R.id.tv_time_updated);
        holder.btnSave = row.findViewById(R.id.img_save_job);
        holder.layoutItemJob = row.findViewById(R.id.layout_item_job);

        row.setTag(holder);
        holder.tvNameJob.setText(holder.job.getNameJob());
        holder.tvCompany.setText(holder.job.getCompany());
        holder.tvPlace.setText(holder.job.getPlace());
        holder.tvSalary.setText(holder.job.getSalary());
        holder.tvTimeUpdated.setText(holder.job.getTime_update());

        pbLoading = ((Activity) context).findViewById(R.id.idLoadingPB);

        hideBtnSave(holder.btnSave, position);

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    toggleJobFavourite(jobList.get(position).getId(), position, holder.btnSave, holder.layoutItemJob);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return row;
    }

    private void hideBtnSave(ImageView imgSaveButton, int position) {
        if (isVisibleBtnSave == false) {
            imgSaveButton.setVisibility(View.GONE);
        }
        if (isSaveView) {
            imgSaveButton.setImageResource(R.drawable.ic_save_job1);
            imgSaveButton.setTag("save");
        }
    }

    private void toggleJobFavourite(String jobId, int position, ImageView imgSave, FrameLayout layoutItem) throws JSONException {

        layoutItem.animate()
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        layoutItem.setVisibility(View.INVISIBLE);
                    }
                });

        layoutItem.setVisibility(View.INVISIBLE);
        mRequestQueue = Volley.newRequestQueue(getContext());
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
//        pbLoading.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
//                    dialogNotification.openDialogNotification(message.substring( 0, message.length() - 1 ), getContext());
//                    pbLoading.setVisibility(View.GONE);
                    if (isSaveView) {
                        listSavedJob.remove(position);
                        jobList.remove(position);
                        imgSave.setTag("not save");
                        notifyDataSetChanged();
                    } else {
                        if (imgSave.getTag().equals("not save")) {
                            imgSave.setImageResource(R.drawable.ic_save_job1);
                            imgSave.setTag("save");
                            listSavedJob.add(jobId);
                        } else {
                            imgSave.setImageResource(R.drawable.ic_save_job);
                            listSavedJob.remove(listSavedJob.size() - 1);
                            imgSave.setTag("not save");
                        }
                        layoutItem.setVisibility(View.VISIBLE);
                    }
                    preferenceManager.putArray(listSavedJob);

                } catch (JSONException e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                    pbLoading.setVisibility(View.GONE);
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

                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Program.TOKEN));
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
        FrameLayout layoutItemJob;
    }

}