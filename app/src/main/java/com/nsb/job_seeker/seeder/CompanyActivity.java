package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.message.activity.ChatActivity;
import com.nsb.job_seeker.message.model.User;
import com.nsb.job_seeker.model.Job;

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
    private TextView tvNameCompany, tvAboutCompany, tvLocationOfCompany, tvTypeCompany, tvPhone, tvAmountEmployer;
    private String IDCompany = "";
    private String url = "https://job-seeker-smy5.onrender.com/job/list/company/";
    private FloatingActionButton fabMessage;
    private String emailReceiver;
    private User userReceive;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_seeker_company);

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
        pbLoading = findViewById(R.id.idLoadingPB);
        fabMessage = findViewById(R.id.fab_messenger);

        jobList = new ArrayList<>();
        userReceive = new User();
    }

    private void setEvent() {
        back();
        getInfoCompany();
        getListJobOfCompany(url);

        openChatBox();
    }

    private void getInfoCompany() {
        pbLoading.setVisibility(View.VISIBLE);
        Bundle bundle = getIntent().getExtras();
        String urlCompany = "https://job-seeker-smy5.onrender.com/company/detail?id=" + bundle.getString("idCompany");

        RequestQueue queue = Volley.newRequestQueue(CompanyActivity.this);

        JsonObjectRequest data = new JsonObjectRequest(Request.Method.GET,
                urlCompany,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response.getJSONObject("data");
                            tvNameCompany.setText(jsonObject.getString("name"));
                            tvAboutCompany.setText(jsonObject.getString("about"));
                            tvLocationOfCompany.setText(jsonObject.getString("location"));
                            tvTypeCompany.setText(jsonObject.getString("type"));
                            tvPhone.setText(jsonObject.getString("phone"));
                            tvAmountEmployer.setText(String.valueOf(jsonObject.getInt("totalEmployee")));
                            emailReceiver = jsonObject.getJSONObject("idUser").getString("email");
                            getInfoUserFirebase();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pbLoading.setVisibility(View.GONE);
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

    private void back() {
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setListViewAdapter() {
        JobAdapter jobAdapter = new JobAdapter(CompanyActivity.this, R.layout.list_view_item_job, jobList, true);
        listView.setAdapter(jobAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(CompanyActivity.this, JobDetailActivity.class);
                i.putExtra("id", jobList.get(position).getId());
                i.putExtra("isApply", true);
                i.putExtra("isLinkCompany", false);
                startActivity(i);
            }
        });
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
                            setListViewAdapter();

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

    private void openChatBox() {
        fabMessage.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Program.KEY_USER, userReceive);
            startActivity(intent);
        });
    }

    private void getInfoUserFirebase() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Program.KEY_COLLECTION_USERS)
                .whereEqualTo(Program.KEY_EMAIL, emailReceiver)
                .get()
                .addOnCompleteListener(task ->{
                    if (task.isSuccessful()) {
                        DocumentSnapshot queryDocumentSnapshot = task.getResult().getDocuments().get(0);
                        userReceive.name = queryDocumentSnapshot.getString(Program.KEY_NAME);
                        userReceive.email = queryDocumentSnapshot.getString(Program.KEY_EMAIL);
                        userReceive.image = queryDocumentSnapshot.getString(Program.KEY_IMAGE);
                        userReceive.token = queryDocumentSnapshot.getString(Program.KEY_FCM_TOKEN);
                        userReceive.id = queryDocumentSnapshot.getId();
                    }
                });
    }
}
