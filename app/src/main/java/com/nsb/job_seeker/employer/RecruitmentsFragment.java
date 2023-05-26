package com.nsb.job_seeker.employer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.RecruitmentAdapter;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.model.Recruitment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class RecruitmentsFragment extends Fragment {
    private ListView listViewRecruitments;
    private List<Recruitment> recruitmentList;
    private View recruitmentView;
    private TextView amountRec;
    private ProgressBar pbLoading;
    private String Url = "https://job-seeker-smy5.onrender.com/job/list/company/";
    private PreferenceManager preferenceManager;
    private RecruitmentAdapter recruitmentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recruitmentView = inflater.inflate(R.layout.fragment_employer_recruitments, container, false);

        setControl();
        setEvent();
        return recruitmentView;
    }

    private void setControl() {
        amountRec = recruitmentView.findViewById(R.id.tv_amount_recruitment);
        listViewRecruitments = recruitmentView.findViewById(R.id.lv_recruitments);
        pbLoading = recruitmentView.findViewById(R.id.idLoadingPB);

        recruitmentList = new ArrayList<Recruitment>();

        preferenceManager = new PreferenceManager(getActivity());
    }


    private void setEvent() {
        getListJobOfCompany(Url);

    }


    private void setListViewAdapter() {
        amountRec.setText("Tổng số lượng tin: " + String.valueOf(recruitmentList.size()));
        if (getActivity()!=null){
            recruitmentAdapter = new RecruitmentAdapter(getActivity(), R.layout.list_view_item_recruitment, recruitmentList);
            listViewRecruitments.setAdapter(recruitmentAdapter);
            listViewRecruitments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(), RecruitmentDetailActivity.class);
                    i.putExtra("id", recruitmentList.get(position).getId());
                    startActivity(i);
                }
            });
        }

    }

    private void getListJobOfCompany(String url) {
        url += preferenceManager.getString(Program.COMPANY_ID);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        pbLoading.setVisibility(View.VISIBLE);
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
                                    recruitmentList.add(new Recruitment(
                                            job.getString("_id"),
                                            job.getString("name"),
                                            job.getString("locationWorking"),
                                            Program.formatTimeDDMMYYYY(job.getString("updateDate")),
                                            "Số lượng hồ sơ: 5",
                                            time
                                    ));
                                }
                            }
                            pbLoading.setVisibility(View.GONE);
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
}
