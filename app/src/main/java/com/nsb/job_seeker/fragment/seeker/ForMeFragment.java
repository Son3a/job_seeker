package com.nsb.job_seeker.fragment.seeker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.activity.seeker.SearchActivity;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentSeekerForMeBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForMeFragment extends Fragment implements JobListener {
    FragmentSeekerForMeBinding binding;
    private PreferenceManager preferenceManager;
    private JobAdapter jobAdapter;
    public static List<Job> jobList;
    private final int RESULT_OK = -1;
    private LoadingDialog loadingDialog;

    private BroadcastReceiver broadcastAvatar = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.BROADCAST_AVATAR.equals(intent.getAction())) {
                String avatar = intent.getStringExtra(Constant.AVATAR);
                binding.imageAvatar.setImageBitmap(Constant.getBitmapFromEncodedString(avatar));
            }
        }
    };

    private BroadcastReceiver broadcastSaveJob = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.BROADCAST_SAVE_JOB.equals(intent.getAction())) {
                Log.d("PositionSave", "ChangeSaveJob");
                int position = intent.getIntExtra(Constant.POSITION, -1);
                if (position != -1) {
                    jobAdapter.notifyItemChanged(position);
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeekerForMeBinding.inflate(getLayoutInflater());
        setControl();
        setEvent();

        return binding.getRoot();
    }

    private void setControl() {
        jobList = new ArrayList<Job>();
        loadingDialog = new LoadingDialog(getContext());
        preferenceManager = new PreferenceManager(getActivity());
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            binding.layoutWelcome.setVisibility(View.VISIBLE);
            binding.layoutLogo.setVisibility(View.INVISIBLE);
        } else {
            binding.layoutWelcome.setVisibility(View.INVISIBLE);
            binding.layoutLogo.setVisibility(View.VISIBLE);
        }
        jobAdapter = new JobAdapter(getContext(), jobList, this, true);
        binding.lvJob.setAdapter(jobAdapter);
    }

    private void setEvent() {
        loadAvatar();
        getNewJobs();
        setStateAppBar();
        gotoSearch();
        refreshContent();
        gotoAccount();
        gotoLogin();
    }

    private void loadAvatar() {
        if (preferenceManager.getString(Constant.AVATAR) != null) {
            if (URLUtil.isValidUrl(preferenceManager.getString(Constant.AVATAR))) {
                Picasso.get().load(preferenceManager.getString(Constant.AVATAR)).into(binding.imageAvatar);
            } else {
                binding.imageAvatar.setImageBitmap(Constant.getBitmapFromEncodedString(preferenceManager.getString(Constant.AVATAR)));
            }
        }

        binding.textName.setText(preferenceManager.getString(Constant.NAME));
        Date now = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(now);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        String time;
        if (hour >= 0 && hour < 6) {
            time = "buổi đêm";
        } else if (hour >= 6 && hour < 12) {
            time = "buổi sáng";
        } else if (hour >= 12 && hour < 18) {
            time = "ngày";
        } else {
            time = "buổi tối";
        }

        binding.textTime.setText(time);
    }

    private void gotoLogin() {
        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void gotoAccount() {
        binding.layoutWelcome.setOnClickListener(v -> {
            if (ForMeFragment.this.isVisible()) {
                Log.d("Forme", "ForMe is showing");
                SeekerMainActivity.bottomNavigationView.setSelectedItemId(R.id.menu_account);
            }
        });
    }

    private void refreshContent() {
        binding.layoutRefresh.setOnRefreshListener(() -> {
            getNewJobs();
        });
    }

    private void gotoSearch() {
        binding.includeSearch.layoutSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });

        binding.includeSearch1.layoutSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        });
    }

    private void getNewJobs() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Constant.url_dev + "/job/list/sort-by-date";
        jobList.clear();
        binding.pbLoading.setVisibility(View.VISIBLE);
        JsonObjectRequest data = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String idCompany = "";
                            JSONArray jobsList = response.getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (job.getString("status").equals("true")) {
                                    jobList.add(new Job(
                                            job.getString("_id"),
                                            job.getString("name"),
                                            job.getJSONObject("idCompany").getString("_id"),
                                            job.getJSONObject("idCompany").getString("name"),
                                            job.getString("locationWorking"),
                                            job.getString("salary"),
                                            job.getString("deadline"),
                                            job.getString("description"),
                                            job.getString("requirement"),
                                            job.getJSONObject("idOccupation").getString("_id"),
                                            job.getJSONObject("idOccupation").getString("name"),
                                            job.getJSONObject("idCompany").getString("image"),
                                            job.getString("amount"),
                                            job.getString("workingForm"),
                                            job.getString("experience"),
                                            job.getString("gender")
                                    ));
                                }
                            }
                            binding.pbLoading.setVisibility(View.GONE);
                            jobAdapter.notifyDataSetChanged();
                            binding.layoutRefresh.setRefreshing(false);
                            //pbLoading.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof com.android.volley.NoConnectionError) {

                        }
                        System.out.println(error);
                    }
                }
        );
        data.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                throw new VolleyError(error.getMessage());
            }
        });
        queue.add(data);
    }

    private void setStateAppBar() {
//        appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
//            if (abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
//                toolbar.setVisibility(View.VISIBLE);
//            } else if (verticalOffset == 0) {
//
//            } else {
//                toolbar.setVisibility(View.INVISIBLE);
//            }
//        });

        binding.layoutNested.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int height = binding.layoutHeader.getHeight();
                if (scrollY >= height) {
                    binding.toolBar.setVisibility(View.VISIBLE);
                    Log.d("Position", scrollX + "  " + scrollY + "  " + height);
                } else {
                    binding.toolBar.setVisibility(View.GONE);
                }

            }
        });
    }

    private void saveJob(String jobId, ListViewItemJobBinding itemJobBinding) throws JSONException {
        String base_url = Constant.url_dev + "/job";
        loadingDialog.showDialog();
        RequestQueue mRequestQueue = Volley.newRequestQueue(getContext());
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    loadingDialog.hideDialog();
                    if (status.equals("1")) {
                        itemJobBinding.imgSaveJob.setImageResource(R.drawable.ic_saved);
                        itemJobBinding.imgSaveJob.setColorFilter(ContextCompat.getColor(getContext(), R.color.green));
                        CustomToast.makeText(getContext(), "Lưu việc làm thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                        Constant.idSavedJobs.add(jobId);
                    } else {
                        itemJobBinding.imgSaveJob.setImageResource(R.drawable.ic_not_save);
                        Constant.idSavedJobs.remove(jobId);
                        itemJobBinding.imgSaveJob.setColorFilter(ContextCompat.getColor(getContext(), R.color.secondary_text));
                        CustomToast.makeText(getContext(), "Bạn đã bỏ lưu công việc!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    loadingDialog.hideDialog();
//                    pbLoading.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                loadingDialog.hideDialog();
                //get status code here
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.data != null) {
                    try {
                        if (error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(getContext(), LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        Toast.makeText(getActivity(), message.substring(1, message.length() - 1), Toast.LENGTH_SHORT).show();
                        //pbLoading.setVisibility(View.GONE);
                        Log.d("ABC", body);
                    } catch (UnsupportedEncodingException e) {
                        //pbLoading.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
                return params;
            }

            ;
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                throw new VolleyError(error.getMessage());
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onClick(Job job, int position) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra(Constant.JOB_ID, job.getId());
        i.putExtra(Constant.POSITION, position);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding listViewItemJobBinding, int position) {
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            try {
                saveJob(job.getId(), listViewItemJobBinding);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Constant.BROADCAST_AVATAR);
        getContext().registerReceiver(broadcastAvatar, intentFilter);

        IntentFilter intentFilter1 = new IntentFilter(Constant.BROADCAST_SAVE_JOB);
        getContext().registerReceiver(broadcastSaveJob, intentFilter1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(broadcastAvatar);
        getContext().unregisterReceiver(broadcastSaveJob);
    }
}
