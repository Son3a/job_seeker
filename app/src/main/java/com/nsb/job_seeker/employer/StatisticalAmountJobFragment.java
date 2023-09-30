package com.nsb.job_seeker.employer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;
import com.nsb.job_seeker.seeker.JobDetailActivity;
import com.nsb.job_seeker.adapter.JobAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticalAmountJobFragment extends Fragment implements JobListener {
    private Spinner spnJob;
    private View statisticalAmountJobView;
    private ProgressBar pbLoading;
    private List<String> nameTypeJobs;
    private List<String> idTypeJobs;
    private List<Job> jobResultList;
    private RecyclerView listView;
    private TextView tvAmountJob;
    private JobAdapter jobAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        statisticalAmountJobView = inflater.inflate(R.layout.fragment_employer_statistical_amount_job, container, false);

        setControl();
        setEvent();

        return statisticalAmountJobView;
    }

    private void setControl() {
        pbLoading = statisticalAmountJobView.findViewById(R.id.idLoadingPB);
        spnJob = statisticalAmountJobView.findViewById(R.id.spinner_job);
        listView = statisticalAmountJobView.findViewById(R.id.lv_job);
        tvAmountJob = statisticalAmountJobView.findViewById(R.id.tv_amount_job);

        nameTypeJobs = new ArrayList<>();
        nameTypeJobs.add("Tất cả");
        idTypeJobs = new ArrayList<>();
        jobResultList = new ArrayList<Job>();

        jobAdapter = new JobAdapter(jobResultList, this, false);
        listView.setAdapter(jobAdapter);
    }

    private void setEvent() {
        getTypeJob();
        showJob();
    }

    private void getTypeJob() {
        String urlTypeJob = "https://job-seeker-smy5.onrender.com/occupation/list";
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, urlTypeJob, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray listTypeJob = response.getJSONObject("data").getJSONArray("data");
                    for (int i = 0; i < listTypeJob.length(); i++) {
                        JSONObject typeJob = listTypeJob.getJSONObject(i);
                        if (typeJob.getString("isDelete").equals("false")) {
                            nameTypeJobs.add(typeJob.getString("name"));
                            idTypeJobs.add(typeJob.getString("_id"));
                        }
                    }
                    bindingDataToSpinner();
                } catch (JSONException e) {
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

    private void bindingDataToSpinner() {
        if (getActivity() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, nameTypeJobs);
            spnJob.setAdapter(adapter);
        }
    }

    private void showJob() {
        spnJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView spinnerText = (TextView) spnJob.getChildAt(0);

                spinnerText.setTextColor(Color.WHITE);
                int index = position - 1;
                if (index < 0) {
                    findJobByIdTypeJob("");
                } else {
                    findJobByIdTypeJob(spnJob.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void findJobByIdTypeJob(String nameTypeJob) {
        pbLoading.setVisibility(View.VISIBLE);
        String url = "https://job-seeker-smy5.onrender.com/job/list/search";
        jobResultList.clear();

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray listJobs = jsonObject.getJSONArray("data");
                    String idCompany = "";
                    for (int i = 0; i < listJobs.length(); i++) {
                        JSONObject job = listJobs.getJSONObject(i);
                        if (job.getString("status").equals("true")) {
                            if (!job.isNull("idCompany")) {
                                idCompany = job.getJSONObject("idCompany").getString("name");
                            } else {
                                idCompany = "";
                            }

                            String time = Program.setTime(job.getString("postingDate"));
                            if (time.equals(null))
                                time = "Vừa mới cập nhật";
                            else
                                time = "Cập nhật " + time + " trước";
                            jobResultList.add(new Job(
                                    job.getString("_id"),
                                    job.getString("name"),
                                    idCompany,
                                    job.getString("locationWorking"),
                                    Program.formatSalary(job.getString("salary")),
                                    time
                            ));
                        }
                    }
                    jobAdapter.notifyDataSetChanged();
                    tvAmountJob.setText("Tìm thấy " + String.valueOf(jobResultList.size()) + " việc làm");
                    pbLoading.setVisibility(View.GONE);
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
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String jsonBody = "{" +
                        "\"key\":\"" + nameTypeJob + "\"}";

                if ((getMethod() == Method.POST) && (jsonBody != null)) {
                    return jsonBody.getBytes();
                } else {
                    return super.getBody();
                }
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
        Intent i = new Intent(getActivity(), JobDetailActivity.class);
        i.putExtra("id", job.getId());
        startActivity(i);
    }

    @Override
    public void onSave(Job job, int position, boolean isSaveView, ListViewItemJobBinding binding) {

    }
}
