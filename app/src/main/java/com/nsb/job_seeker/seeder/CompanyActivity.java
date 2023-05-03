package com.nsb.job_seeker.seeder;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.model.Job;
import com.nsb.job_seeker.model.Recruitment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CompanyActivity extends AppCompatActivity {
    private ListView listView;
    private ProgressBar pbLoading;
    private List<Job> jobList;
    private ImageView icBack;
    private TextView tvNameCompany, tvAboutCompany, tvLocationOfCompany, tvTypeCompany,tvPhone, tvAmountEmployer;
    private String IDCompany = "";
    private String url = "https://job-seeker-smy5.onrender.com/job/list/company/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_list_job_by_company);

        setControl();
        setEvent();
    }

    private void setControl() {
        listView = findViewById(R.id.lv_job_by_company);
        pbLoading = findViewById(R.id.idLoadingPB);
        icBack = findViewById(R.id.ic_back);
        tvNameCompany = findViewById(R.id.tv_name_company);
        tvAboutCompany = findViewById(R.id.tv_about_company);
        tvLocationOfCompany = findViewById(R.id.tv_location_company);
        tvTypeCompany = findViewById(R.id.tv_type_company);
        tvPhone = findViewById(R.id.tv_phone_company);
        tvAmountEmployer = findViewById(R.id.tv_amount_employer);

        jobList = new ArrayList<>();
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));
        jobList.add(new Job("Lap trinh Mobile", "FPT", "Ho Chi Minh", "20.0000", "Cap nhat 1 thang"));

    }

    private void setEvent() {
        back();
        setListView();
    }

    private void back() {
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setListView() {
        ListViewApdapter listViewApdapter = new ListViewApdapter(CompanyActivity.this, R.layout.list_view_item_job, jobList, true);
        listView.setAdapter(listViewApdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                Intent i = new Intent(ListJobByCompany.this, JobDetailActivity.class);
//                i.putExtra("id", jobList.get(position).getId());
//                i.putExtra("isApply", true);
//                startActivity(i);
            }
        });
    }

    private void getInforCompany(String url){

    }

    private void getListJobOfCompany(String url) {
        Bundle bundle = getIntent().getExtras();
        IDCompany = bundle.getString("idCompany");
        url += IDCompany;

        RequestQueue queue = Volley.newRequestQueue(CompanyActivity.this);

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String idCompany = "";
                            JSONArray jobsList = response.getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (job.getString("status").equals("true")) {
                                    String time = Program.setTime(job.getString("deadline"));

                                    if (time.equals(null)) {
                                        time = "Hết hạn";
                                    } else {
                                        time = "Còn " + time;
                                    }
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
//                            setListViewAdapter();

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

            }
        });
        queue.add(data);
    }
}
