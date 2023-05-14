package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.regex.Pattern;

public class JobDetailActivity extends AppCompatActivity {
    private ImageView imgBack;
    private Button btnApply;
    private TextView tvSalary, tvTypeJob, tvTimeJob, tvExperience, tvTimeUpdated, tvAddress, tvDescJob, tvNameJob, tvCompany, tvPlace, tvReqSkill;
    private ProgressBar pbLoading;
    private RelativeLayout body;
    private String IDCompany = "";
    private String IDJob = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_job_detail);

        setControl();


        getJobDetail();
        setEvent();
    }

    private void setControl() {
        imgBack = findViewById(R.id.ic_back);
        btnApply = findViewById(R.id.btn_apply);

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
        tvReqSkill = findViewById(R.id.tv_request_skill);
        pbLoading = findViewById(R.id.idLoadingPB);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("isLinkCompany")) {
            tvCompany.setEnabled(false);
        }
        IDJob = bundle.getString("id");
    }

    private void setEvent() {
        back();

        changeBtnSubmit();

        applyJob();

        directToListJobByCompany();
    }

    private void back() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void applyJob() {
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
                i.putExtra("idJob",IDJob);
                startActivity(i);
            }
        });
    }

    private void changeBtnSubmit() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("isApply")) {
            boolean isApply = bundle.getBoolean("isApply");
            if (!isApply) {
                btnApply.setText("ĐÃ ỨNG TUYỂN");
                btnApply.setEnabled(false);
            } else {
                btnApply.setText("ỨNG TUYỂN NGAY");
            }
        } else {
            btnApply.setVisibility(View.GONE);
        }
    }

    private void getJobDetail() {
        RequestQueue requestQueue = Volley.newRequestQueue(JobDetailActivity.this);

        String url = "https://job-seeker-smy5.onrender.com/job/detail?id=" + IDJob;
        pbLoading.setVisibility(View.VISIBLE);

        JsonObjectRequest data = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject job = response.getJSONObject("data");

                    String idCompany = "", place = "";
                    if (!job.isNull("idCompany")) {
                        idCompany = job.getJSONObject("idCompany").getString("name");
                        place = job.getJSONObject("idCompany").getString("location");
                        IDCompany = job.getJSONObject("idCompany").getString("_id");
                    }

                    String typeJob = "";
                    if (!job.isNull("idOccupation")) {
                        typeJob = job.getJSONObject("idOccupation").getString("name");
                    }

                    String salary = job.getString("salary");

                    salary = Program.formatSalary(salary.replaceAll("\\D+", ""));

                    String time = Program.setTime(job.getString("updateDate"));
                    if (time.equals(null))
                        time = "Vừa mới cập nhật";
                    else
                        time = "Cập nhật " + time + " trước";

                    tvNameJob.setText(job.getString("name"));
                    tvCompany.setText(idCompany);
                    tvPlace.setText(place);
                    tvSalary.setText(salary);
                    tvTypeJob.setText(typeJob);
                    tvTimeJob.setText(job.getString("hourWorking"));
                    tvExperience.setText("Không cần kinh nghiệm");
                    tvTimeUpdated.setText(time);
                    tvAddress.setText("• " + job.getString("locationWorking"));
                    tvDescJob.setText(Program.formatStringToBullet(job.getString("description")));
                    tvReqSkill.setText(Program.formatStringToBullet(job.getString("requirement")));

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


    private void directToListJobByCompany() {
        tvCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(JobDetailActivity.this, IDCompany, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(JobDetailActivity.this, CompanyActivity.class);
                i.putExtra("idCompany", IDCompany);
                startActivity(i);
            }
        });
    }
}
