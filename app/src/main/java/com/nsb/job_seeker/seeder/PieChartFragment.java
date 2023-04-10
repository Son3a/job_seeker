package com.nsb.job_seeker.seeder;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nsb.job_seeker.R;

import java.util.ArrayList;

public class PieChartFragment extends Fragment {

    private View pieChartView;
    private PieChart pieChart;

    private ListView listViewJob;
    private String[] listNameJobs;
    private int[] listAmountJobs;
    private int sum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        pieChartView = inflater.inflate(R.layout.fragment_seeker_piechart, container, false);

        setControl();
        setEvent();
        return pieChartView;
    }


    private void setControl() {
        sum = 0;
        pieChart = pieChartView.findViewById(R.id.piechart_job);
        listViewJob = pieChartView.findViewById(R.id.lv_job);

        listNameJobs = new String[]{"Reactjs", "Nodejs", "Python", "Java","Machine Learning", "Data Analyst", "Business Analyst", "Devops"};
        listAmountJobs = new int[]{12, 16, 20, 8, 22, 5, 9, 8};
    }

    private void setEvent() {
        setupPieChart();
        loadPieChart();

        StatisticalPieChartAdapter statisticalPieChartAdapter = new StatisticalPieChartAdapter(getActivity(), R.layout.list_view_item_percent_job, listNameJobs, listAmountJobs, sum);
        listViewJob.setAdapter(statisticalPieChartAdapter);
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

        for (int i = 0; i < listNameJobs.length; i++) {
            pieChartArrayList.add(new PieEntry(listAmountJobs[i], listNameJobs[i]));
            sum += listAmountJobs[i];
        }

        ArrayList<Integer> colors = new ArrayList<>();
        for(int color:ColorTemplate.MATERIAL_COLORS){
            colors.add(color);
        }
        for(int color:ColorTemplate.VORDIPLOM_COLORS){
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
}
