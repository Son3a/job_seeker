package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class JobDetailActivity extends AppCompatActivity {
    private ImageView imgBack;
    private Button btnApply, btnCancel;
    private TextView tvSalary, tvTypeJob, tvTimeJob, tvExperience, tvTimeUpdated, tvAddress, tvDescJob, tvNameJob, tvCompany, tvPlace;
    private GridView gridViewSkill;
    private ProgressBar pbLoading;
    private RelativeLayout body;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_job_detail);

        setControl();
        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString("id");
        boolean isApply = bundle.getBoolean("isApply");
        if (isApply) {
            btnApply.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);
        } else {
            btnApply.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
        }
        String url = "https://job-seeker-smy5.onrender.com/job/detail?id=" + id;

        callAPI(url);
        setEvent();
    }

    private void setControl() {
        imgBack = findViewById(R.id.ic_back);
        btnApply = findViewById(R.id.btn_apply);
        btnCancel = findViewById(R.id.btn_cancel);
        gridViewSkill = findViewById(R.id.gv_skill);

        tvSalary = findViewById(R.id.txt_salary_detail);
        tvTypeJob = findViewById(R.id.txt_type_job_detail);
        tvTimeJob = findViewById(R.id.txt_type_job_time_detail);
        tvExperience = findViewById(R.id.txt_experience_detail);
        tvTimeUpdated = findViewById(R.id.txt_time_updated_detail);
        tvAddress = findViewById(R.id.tv_address_detail);
        tvDescJob = findViewById(R.id.tv_desc_job_detail);
        tvNameJob = findViewById(R.id.txt_name_job_detail);
        tvCompany = findViewById(R.id.txt_company_detail);
        tvPlace = findViewById(R.id.txt_place_detail);
        body = findViewById(R.id.body_layout);
        pbLoading = findViewById(R.id.idLoadingPB);
    }

    private void setEvent() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
                startActivity(i);
            }
        });

        directToListJobByCompany();
    }

    private void callAPI(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(JobDetailActivity.this);
        pbLoading.setVisibility(View.VISIBLE);
        System.out.println("Logggggggggggggggggggggggggggggggggg");
        JsonObjectRequest data = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("Loggggggggggggggggggggggggggggggggg2");
                try {
                    JSONObject job = response.getJSONObject("data");

                    String idCompany = "", place = "";
                    if (!job.isNull("idCompany")) {
                        idCompany = job.getJSONObject("idCompany").getString("name");
                        place = job.getJSONObject("idCompany").getString("location");
                    }

                    String typeJob = "";
                    if (!job.isNull("idOccupation")) {
                        typeJob = job.getJSONObject("idOccupation").getString("name");
                    }

                    String salary = job.getString("salary");
                    if (Pattern.matches("[a-zA-Z]+", salary) == false && salary.length() > 2) {
                        salary = Program.formatSalary(salary);
                    }
                    String[] listSkill = job.getString("requirement").split(",");
                    setViewSkillReq(listSkill);

                    String time = Program.setTime(job.getString("postingDate"));
                    if(time.equals(null))
                        time = "Vừa mới cập nhật";
                    else
                        time = "Cập nhật " + time + " trước";

                    tvNameJob.setText(job.getString("name"));
                    tvCompany.setText(idCompany);
                    tvPlace.setText(place);
                    tvSalary.setText("VND " + salary);
                    tvTypeJob.setText(typeJob);
                    tvTimeJob.setText(job.getString("hourWorking"));
                    tvExperience.setText("Không cần kinh nghiệm");
                    tvTimeUpdated.setText(time);
                    tvAddress.setText("\u25CF    " + job.getString("locationWorking"));
                    tvDescJob.setText("\u25CF    " + job.getString("description"));

                    pbLoading.setVisibility(View.GONE);
                    body.setVisibility(View.VISIBLE);

                    //setContentView(R.layout.activity_seeker_job_detail);

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
        });
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
        requestQueue.add(data);
    }

    private void setViewSkillReq(String[] skillList) {
        ListViewSkillApdapter listViewSkillApdapter = new ListViewSkillApdapter(JobDetailActivity.this, R.layout.list_view_skill, skillList);
        gridViewSkill.setAdapter(listViewSkillApdapter);
    }


    private void directToListJobByCompany() {
        tvCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(JobDetailActivity.this, ListJobByCompany.class);
                startActivity(i);
//                Toast.makeText(JobDetailActivity.this, "hihi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
