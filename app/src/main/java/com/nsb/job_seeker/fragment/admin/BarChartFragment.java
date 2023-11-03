package com.nsb.job_seeker.fragment.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.nsb.job_seeker.R;

import java.util.ArrayList;

public class BarChartFragment extends Fragment {
    private View barChartView;
    private BarChart barChart;
    private int[] listValues;
    private String[] listLabels;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        barChartView = inflater.inflate(R.layout.fragment_seeker_barchart, container, false);
        setControl();
        setEvent();
        setupBarChart();
        return barChartView;
    }

    private void setControl() {
        barChart = barChartView.findViewById(R.id.barchart);
        listValues = new int[]{420, 475, 508, 660, 550, 630, 475};
        listLabels = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    }

    private void setEvent() {

        ArrayList<BarEntry> visitors = new ArrayList<>();
        for (int i = 0; i < listValues.length; i++) {
            visitors.add(new BarEntry(i, listValues[i]));
        }

        BarDataSet barDataSet = new BarDataSet(visitors, "");
        barDataSet.setColors(Color.BLUE);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);

        barChart.setFitBars(true);
        barChart.setData(barData);
        barChart.getDescription().setText("");

    }

    private void setupBarChart() {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(listLabels));

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(listValues.length);
//        xAxis.setLabelRotationAngle(270); set direction label

        YAxis yLabels = barChart.getAxisRight();
        yLabels.setEnabled(false);


        barChart.animateY(2000);
        barChart.invalidate();
    }
}
