package com.nsb.job_seeker.employer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class RecruitmentDetailActivity extends AppCompatActivity {
    private ImageView icBack, icListCV, icDelete, icEdit;
    private ProgressBar pbLoading;
    private TextView tvSalary, tvTypeJob, tvTimeWork, tvTimeUpdated, tvDescJob, tvNameJob, tvPlace, tvDeadLine, tvJobReq;
    private ScrollView layoutBody;

    private String url = "https://job-seeker-smy5.onrender.com/job/detail?id=";
    private String idJob = "";

    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recruitment_detail);


        setControl();
        setEvent();
    }

    private void setControl() {
        icBack = findViewById(R.id.ic_back_rec_detail);
        icListCV = findViewById(R.id.ic_list_cv);
        icDelete = findViewById(R.id.ic_delete_recruitment);
        icEdit = findViewById(R.id.ic_edit_recruitment);
        pbLoading = findViewById(R.id.idLoadingPB);

        tvNameJob = findViewById(R.id.tv_name_job_rec);
        tvPlace = findViewById(R.id.tv_place);
        tvSalary = findViewById(R.id.tv_salary);
        tvTimeWork = findViewById(R.id.tv_work_time);
        tvTypeJob = findViewById(R.id.tv_type_job);
        tvTimeUpdated = findViewById(R.id.tv_time_created);
        tvDeadLine = findViewById(R.id.tv_deadline);
        tvDescJob = findViewById(R.id.tv_detail_job);
        tvJobReq = findViewById(R.id.tv_job_req);
        layoutBody = findViewById(R.id.layout_recruitment_detail);

        preferenceManager = new PreferenceManager(this);
    }

    private void setEvent() {
        Bundle bundle = getIntent().getExtras();
        idJob = bundle.getString("id");
        url += idJob;

        back();

        getRecruitment();

        clickDeleteRecruitment();
        clickEditRecruitment();
        showListCV();
    }

    private void back(){
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getIntent().getExtras();
                if (bundle.containsKey("isChange")) {
                    Intent i = new Intent(RecruitmentDetailActivity.this, EmployerMainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }

                onBackPressed();
            }
        });
    }

    private void getRecruitment() {
        RequestQueue requestQueue = Volley.newRequestQueue(RecruitmentDetailActivity.this);
        pbLoading.setVisibility(View.VISIBLE);
        JsonObjectRequest data = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject job = response.getJSONObject("data");

                    String place = "";
                    if (!job.isNull("idCompany")) {
                        place = job.getString("locationWorking");
                    }

                    String typeJob = "";
                    if (!job.isNull("idOccupation")) {
                        typeJob = job.getJSONObject("idOccupation").getString("name");
                    }

                    String salary = job.getString("salary");
//                    if (Pattern.matches("[a-zA-Z]+", salary) == false) {
//                        salary = "VND " + Program.formatSalary(salary);
//                    }
                    salary = Program.formatSalary(salary.replaceAll("\\D+", ""));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");

                    String timeUpdated = output.format(sdf.parse(job.getString("updateDate")));
                    String deadLine = output.format(sdf.parse(job.getString("deadline")));

                    tvNameJob.setText(job.getString("name"));
                    tvPlace.setText(place);
                    tvSalary.setText(salary);
                    tvTimeWork.setText(job.getString("hourWorking"));
                    tvTypeJob.setText(typeJob);
                    tvTimeUpdated.setText("Cập nhật ngày " + timeUpdated);
                    tvDeadLine.setText(deadLine);
                    tvDescJob.setText(Program.formatStringToBullet(job.getString("description")));
                    tvJobReq.setText(Program.formatStringToBullet(job.getString("requirement")));

                    pbLoading.setVisibility(View.GONE);
                    layoutBody.setVisibility(View.VISIBLE);
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

    private void clickDeleteRecruitment() {
        icDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDelete("Bạn có muốn xóa tin tuyển dụng này không ?");
            }
        });
    }

    private void deleteRecruitmentAPI() throws JSONException {
        String urlDeleteRecruitment ="https://job-seeker-smy5.onrender.com/job/delete";
        String access_token = preferenceManager.getString(Program.TOKEN);
        Log.d("ABC", "check token : " + access_token);
        RequestQueue queue = Volley.newRequestQueue(RecruitmentDetailActivity.this);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("_id", idJob);
        Log.d("ABC", "idJOb "+idJob);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.PATCH, urlDeleteRecruitment, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(RecruitmentDetailActivity.this, "Xóa thành công!!!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(RecruitmentDetailActivity.this, EmployerMainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                System.out.println(error);
//                Log.d("ABC","check error : " + error.toString());
                String body;
                //get status code here
//                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if(error.networkResponse.data!=null) {
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        Log.d("ABC", body);
                    } catch (UnsupportedEncodingException e) {
                        Log.d("ABC","LOI 2 : " + e.toString());
                        e.printStackTrace();
                    }
//                    loadingDialog.dismissDialog();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", access_token);
                return headers;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    private void clickEditRecruitment() {
        icEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecruitmentDetailActivity.this, EditRecruitmentActivity.class);
                i.putExtra("id", idJob);
                i.putExtra("nameJob", tvNameJob.getText());
                i.putExtra("place", tvPlace.getText());
                i.putExtra("salary", tvSalary.getText());
                i.putExtra("timeWork", tvTimeWork.getText());
                i.putExtra("typeJob", tvTypeJob.getText());
                i.putExtra("deadline", tvDeadLine.getText());
                i.putExtra("detailJob", Program.formatStringFromBullet(tvDescJob.getText().toString()));
                i.putExtra("jobReq", Program.formatStringFromBullet(tvJobReq.getText().toString()));
                startActivity(i);
            }
        });
    }

    private void showListCV() {
        icListCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecruitmentDetailActivity.this, ListCVActivity.class);
                i.putExtra("id",idJob);
                startActivity(i);
            }
        });
    }

    private void showDialogDelete(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(RecruitmentDetailActivity.this)
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            deleteRecruitmentAPI();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).create();
        dialog.show();
    }
}
