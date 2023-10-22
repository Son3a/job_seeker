package com.nsb.job_seeker.seeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.ExperienceAdapter;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.databinding.ActivitySeekerSearchResultBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultActivity extends AppCompatActivity implements JobListener {
    private ActivitySeekerSearchResultBinding binding;
    private List<Job> jobList;
    private JobAdapter jobAdapter;
    private View layoutBottomExperience, layoutBottomSalary, layoutBottomPosition;
    private BottomSheetDialog bottomSheetExperience, bottomSheetSalary, bottomSheetPosition;
    private List<String> listExperiences, listSalary, listPosition;
    private RecyclerView recyclerViewExperience, recyclerViewSalary, recyclerViewPosition;
    private ExperienceAdapter experienceAdapter, positionAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivitySeekerSearchResultBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        init();
        setEvent();
    }

    private void init() {
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList, this, true);
        //binding.rcvResultSearch.setAdapter(jobAdapter);

        initBottomSheetSalary();
        initBottomSheetExperience();
        initBottomSheetPosition();
    }

    private void setEvent() {
        String key = getIntent().getStringExtra("Keyword");
        binding.textKeySearch.setText(key);

        //findJob(key);
        getProvinceApi();
        openBottomExperience();
        openBottomSalary();
        openBottomPosition();
    }

    private void initBottomSheetPosition() {
        listPosition = new ArrayList<>();
        listPosition.add("Tất cả");
        layoutBottomPosition = getLayoutInflater().inflate(R.layout.layout_position, null);
        positionAdapter = new ExperienceAdapter(listPosition);
        recyclerViewPosition = layoutBottomPosition.findViewById(R.id.rcvPosition);
        recyclerViewPosition.setAdapter(positionAdapter);
        bottomSheetPosition = new BottomSheetDialog(this);
        bottomSheetPosition.setContentView(layoutBottomPosition);
    }

    private void initBottomSheetExperience() {
//        listExperiences = new ArrayList<>();
////        listExperiences.add("Tất cả");
////        listExperiences.add("Không yêu cầu");
////        listExperiences.add("Dưới 1 năm");
////        listExperiences.add("1 năm");
////        listExperiences.add("2 năm");
////        listExperiences.add("3 năm");
////        listExperiences.add("4 năm");
////        listExperiences.add("5 năm");
////        listExperiences.add("Trên 5 năm");
//        listExperiences.add("1");
//        listExperiences.add("2");
//        listExperiences.add("3");
//        listExperiences.add("4");
//        listExperiences.add("5");
//        listExperiences.add("6");
//        listExperiences.add("7");
//        listExperiences.add("8");
//        listExperiences.add("9");
//        listExperiences.add("10");
//        listExperiences.add("11");
//        listExperiences.add("12");
//        listExperiences.add("13");
//        listExperiences.add("14");
//
//        layoutBottomExperience = getLayoutInflater().inflate(R.layout.layout_experience, null);
////        bottomSheetExperience = new BottomSheetDialog(this);
////        bottomSheetExperience.setContentView(layoutBottomExperience);
////        bottomSheetExperience.setCancelable(true);
////
////        experienceAdapter = new ExperienceAdapter(listExperiences);
////        recyclerViewExperience = layoutBottomExperience.findViewById(R.id.rcvExperience);
////        recyclerViewExperience.setAdapter(experienceAdapter);
//        ScrollPickerView scrollPickerView = layoutBottomExperience.findViewById(R.id.scrollpickerview);
//        ScrollPickerAdapter scrollPickerAdapter = new ScrollPickerAdapter(listExperiences, R.layout.layout_experience,1);
//        ScrollPickerView.Builder(scrollPickerView).scr
    }

    private void initBottomSheetSalary() {
        listSalary = new ArrayList<>();
        listSalary.add("Tất cả");
        listSalary.add("Dưới 10 triệu");
        listSalary.add("10 - 15 triệu");
        listSalary.add("15 - 20 triệu");
        listSalary.add("20 - 25 triệu");
        listSalary.add("25 - 30 triệu");
        listSalary.add("30 - 50 triệu");
        listSalary.add("Trên 50 triệu");
        listSalary.add("Thỏa thuận");

        layoutBottomSalary = getLayoutInflater().inflate(R.layout.layout_salary, null);
        ExperienceAdapter experienceAdapter1 = new ExperienceAdapter(listSalary);
        recyclerViewSalary = layoutBottomSalary.findViewById(R.id.rcvSalary);
        recyclerViewSalary.setAdapter(experienceAdapter1);
        bottomSheetSalary = new BottomSheetDialog(this);
        bottomSheetSalary.setContentView(layoutBottomSalary);
    }

    private void test() {

    }

    private void openBottomPosition() {
        binding.textPosition.setOnClickListener(v -> {
            bottomSheetPosition.show();
        });
    }

    private void openBottomExperience() {
        binding.textExperience.setOnClickListener(v -> {
            bottomSheetExperience.show();
        });
    }

    private void openBottomSalary() {
        binding.textSalary.setOnClickListener(v -> {
            bottomSheetSalary.show();
        });
    }

    private void getProvinceApi() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://provinces.open-api.vn/api/";

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        String data = jsonObject.getString("name");
                        String newData = data.replace("Tỉnh", "");
                        listPosition.add(newData.replace("Thành phố", ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                positionAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
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
                System.out.println(error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void findJob(String key) throws JSONException {
        binding.pbLoading.setVisibility(View.VISIBLE);
        String url = "https://job-seeker-smy5.onrender.com/job/list/search";

        String idOccupation[] = new String[0];
        String locationWorking[] = new String[0];

//        if (positionSpnLocation != 0) {
//            locationWorking = new String[]{spinnerLocation.getSelectedItem().toString()};
//        }
//
//        if ((positionSpnIdOccupation - 1) >= 0) {
//            idOccupation = new String[]{idTypeJobList.get(positionSpnIdOccupation - 1)};
//        }

        RequestQueue queue = Volley.newRequestQueue(this);


        JSONArray idOccupationParam = new JSONArray(idOccupation);
        JSONArray locationWorkingParam = new JSONArray(locationWorking);

        JSONObject params = new JSONObject();
        params.put("key", key);
        params.put("locationWorking", locationWorkingParam);
        params.put("idOccupation", idOccupationParam);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray listJobs = response.getJSONArray("data");
                    String idCompany = "";
                    if (listJobs.length() == 0) {
//                        tvNote.setText("Kết quả tìm kiếm không có");
//                        tvNote.setVisibility(View.VISIBLE);
//                        listView.setVisibility(View.GONE);
//                        pbLoading.setVisibility(View.GONE);
                        return;
                    }
                    binding.textAmountResult.setText(String.valueOf(listJobs.length()));
                    for (int i = 0; i < listJobs.length(); i++) {
                        JSONObject job = listJobs.getJSONObject(i);
                        if (job.getString("status").equals("true")) {

                            if (!job.isNull("idCompany")) {
                                idCompany = job.getJSONObject("idCompany").getString("name");
                            } else {
                                idCompany = "";
                            }

                            String time = Program.setTime(job.getString("updateDate"));
                            if (time.equals(null))
                                time = "Vừa mới cập nhật";
                            else
                                time = "Cập nhật " + time + " trước";
                            jobList.add(new Job(
                                    job.getString("_id"),
                                    job.getString("name"),
                                    idCompany,
                                    job.getString("locationWorking"),
                                    job.getString("salary"),
                                    time
                            ));
                        }
                    }
                    jobAdapter.notifyDataSetChanged();
                    binding.pbLoading.setVisibility(View.GONE);
                    System.out.println("Create Successful!!!");

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        sr.setRetryPolicy(new RetryPolicy() {
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
                System.out.println(error);
            }
        });
        queue.add(sr);
    }

    @Override
    public void onClick(Job job) {
        Intent i = new Intent(this, JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", true);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, int position, boolean isSaveView, ListViewItemJobBinding binding) {

    }
}
