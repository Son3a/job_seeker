package com.nsb.job_seeker.employer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.StatisticalPieChartAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PieChartFragment extends Fragment {

    private View pieChartView;
    private PieChart pieChart;

    private ListView listViewJob;
    private ArrayList<String> listNameJobs;
    private ArrayList<Integer> listAmountJobs;
    private int sum;
    private ProgressBar pbLoading;
    private String url = "https://job-seeker-smy5.onrender.com/job/statistical/application-by-occupation";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        pieChartView = inflater.inflate(R.layout.fragment_seeker_piechart, container, false);

        setControl();
        callApi(url);
        return pieChartView;
    }


    private void setControl() {
        sum = 0;
        pieChart = pieChartView.findViewById(R.id.piechart_job);
        listViewJob = pieChartView.findViewById(R.id.lv_job);
        pbLoading = pieChartView.findViewById(R.id.idLoadingPB);

        listNameJobs = new ArrayList<>();
        listAmountJobs = new ArrayList<>();
    }

    private void setEvent() {

        callApi(url);
    }


    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("My Job");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
        l.setTextSize(16f);
    }

    private void loadPieChart() {
        ArrayList<PieEntry> pieChartArrayList = new ArrayList<>();

        for (int i = 0; i < listNameJobs.size(); i++) {
            pieChartArrayList.add(new PieEntry(listAmountJobs.get(i), listNameJobs.get(i)));
            sum += listAmountJobs.get(i);
        }

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet pieDataSet = new PieDataSet(pieChartArrayList, "");
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(true);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }

    private void callApi(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        pbLoading.setVisibility(View.VISIBLE);

        System.out.println("logggggggggggggggggggggggggggggggggg");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("logggggggggggggggggggggggggggggggggg");
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String tempAmount = jsonArray.getJSONObject(i).getString("count");
                                String tempName = jsonArray.getJSONObject(i).getString("name");

                                if (!tempAmount.equals("0")) {
                                    listAmountJobs.add(Integer.parseInt(tempAmount));
                                    listNameJobs.add(tempName);
                                }
                            }
                            pbLoading.setVisibility(View.GONE);

                            setupPieChart();
                            loadPieChart();
                            if (getActivity() != null) {
                                StatisticalPieChartAdapter statisticalPieChartAdapter = new StatisticalPieChartAdapter(getActivity(), R.layout.list_view_item_percent_job, listNameJobs, listAmountJobs, sum);
                                listViewJob.setAdapter(statisticalPieChartAdapter);
                            }
                        } catch (JSONException e) {
                            System.out.println(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
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

            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
