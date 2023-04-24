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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.seeder.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecruitmentsFragment extends Fragment {
    private RecruitmentsListViewAdapter recruitmentsListViewAdapter;
    private ListView listViewRecruitments;
    private List<Recruitment> recruitmentList;
    private View recruitmentView;
    private TextView amountRec;
    private ProgressBar pbLoading;
//    private String url = "https://job-seeker-smy5.onrender.com/job/update";
    private String url = "https://job-seeker-smy5.onrender.com/job/list";

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
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
        recruitmentList.add(new Recruitment("Junior PHP Developer", "Hồ Chí Minh", "12/02/2023", "Số lượng hồ sơ: 5", "còn 4 ngày"));
    }


    private void setEvent() {
        setListViewAdapter();

        amountRec.setText("Tổng số lượng tin: " + String.valueOf(recruitmentList.size()));
    }

    private void setListViewAdapter() {
        recruitmentsListViewAdapter = new RecruitmentsListViewAdapter(getActivity(), R.layout.list_view_item_recruitment, recruitmentList);
        listViewRecruitments.setAdapter(recruitmentsListViewAdapter);
        listViewRecruitments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), RecruitmentDetailActivity.class);
                startActivity(i);
            }
        });
    }

    private void callAPI(String url) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        pbLoading.setVisibility(View.VISIBLE);
        JsonObjectRequest data = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {try {
                            String idCompany = "";
                            JSONArray jobsList = response.getJSONObject("data").getJSONArray("data");
                            for (int i = 0; i < jobsList.length(); i++) {
                                JSONObject job = jobsList.getJSONObject(i);
                                if (!job.isNull("idCompany")) {
                                    idCompany = job.getJSONObject("idCompany").getString("name");
                                } else {
                                    idCompany = "";
                                }
                                recruitmentList.add(new Recruitment(

                                ));

                            }
                            pbLoading.setVisibility(View.GONE);
                            setEvent();
                        } catch (JSONException e) {
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
