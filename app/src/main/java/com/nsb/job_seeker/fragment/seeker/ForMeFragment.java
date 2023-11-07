package com.nsb.job_seeker.fragment.seeker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.type.DateTime;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.seeker.JobDetailActivity;
import com.nsb.job_seeker.activity.seeker.SearchActivity;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentSeekerForMeBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constant.BROADCAST_AVATAR.equals(intent.getAction())){
                String avatar = intent.getStringExtra(Constant.AVATAR);
                binding.layoutWelcome.imageAvatar.setImageBitmap(Constant.getBitmapFromEncodedString(avatar));
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
        preferenceManager = new PreferenceManager(getActivity());
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
    }

    private void loadAvatar() {
        if (preferenceManager.getString(Constant.AVATAR) != null) {
            binding.layoutWelcome.imageAvatar.setImageBitmap(Constant.getBitmapFromEncodedString(preferenceManager.getString(Constant.AVATAR)));
        }

        binding.layoutWelcome.textName.setText(preferenceManager.getString(Constant.NAME));
        Date now = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(now);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        String time;
        if(hour >= 0 && hour < 6){
            time = "buổi đêm";
        } else if(hour >= 6 && hour < 12){
            time = "buổi sáng";
        } else if(hour >= 12 && hour < 18){
            time = "ngày";
        } else {
            time = "buổi tối";
        }

        binding.layoutWelcome.textTime.setText(time);
    }

    private void gotoAccount() {
        binding.layoutWelcome.getRoot().setOnClickListener(v -> {
            SeekerMainActivity.bottomNavigationView.setSelectedItemId(R.id.menu_account);
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
                                            job.getJSONObject("idCompany").getString("name"),
                                            job.getString("locationWorking"),
                                            job.getString("salary"),
                                            Constant.setTime(job.getString("deadline")),
                                            job.getString("description"),
                                            job.getString("requirement"),
                                            job.getJSONObject("idOccupation").getString("name"),
                                            job.getJSONObject("idCompany").getString("image"),
                                            job.getString("amount"),
                                            job.getString("working_form"),
                                            job.getString("experience"),
                                            job.getString("gender")
                                    ));
                                }
                            }
                            jobAdapter.notifyDataSetChanged();
                            binding.layoutRefresh.setRefreshing(false);
                            //pbLoading.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
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
        itemJobBinding.layoutItemJob.animate()
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        itemJobBinding.layoutItemJob.setVisibility(View.INVISIBLE);
                    }
                });

        itemJobBinding.layoutItemJob.setVisibility(View.INVISIBLE);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getContext());
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
//        pbLoading.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");

                    if (status.equals("1")) {
                        itemJobBinding.imgSaveJob.setImageResource(R.drawable.ic_saved);
                        itemJobBinding.imgSaveJob.setColorFilter(ContextCompat.getColor(getContext(), R.color.green));
                        Constant.idSavedJobs.add(jobId);
                    } else {
                        itemJobBinding.imgSaveJob.setImageResource(R.drawable.ic_not_save);
                        Constant.idSavedJobs.remove(Constant.idSavedJobs.size() - 1);
                        itemJobBinding.imgSaveJob.setColorFilter(ContextCompat.getColor(getContext(), R.color.secondary_text));
                        CustomToast.makeText(getContext(), "Bạn đã bỏ lưu công việc!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    }

                    itemJobBinding.layoutItemJob.setVisibility(View.VISIBLE);
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
    public void onClick(Job job) {
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", true);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding listViewItemJobBinding) {
        try {
            saveJob(job.getId(), listViewItemJobBinding);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Constant.BROADCAST_AVATAR);
        getContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(broadcastReceiver);
    }
}
