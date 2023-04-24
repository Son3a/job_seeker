package com.nsb.job_seeker.employer;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.R;
import com.nsb.job_seeker.seeder.BarChartFragment;

public class StatisticalJobActivity extends AppCompatActivity {
    private ImageView icExpend, icBack;
    private BarChartFragment barChartFragment;
    private PieChartFragment pieChartFragment;
    private StatisticalAmountJobFragment statisticalAmountJobFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_chart);

        setControl();
        setEvent();
    }

    private void setControl() {
        icExpend = (ImageView) findViewById(R.id.ic_expend);
        icBack = findViewById(R.id.ic_back);
        barChartFragment = new BarChartFragment();
        pieChartFragment = new PieChartFragment();
        statisticalAmountJobFragment = new StatisticalAmountJobFragment();
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, pieChartFragment).commit();

        icExpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragmentInFrame = getSupportFragmentManager().findFragmentById(R.id.container_chart);
                PopupMenu popupMenu = new PopupMenu(StatisticalJobActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_statistical_piechart, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.bar_chart) {
                            popupMenu.getMenuInflater().inflate(R.menu.menu_statistical_barchart, popupMenu.getMenu());
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, barChartFragment).commit();
                            return true;
                        } else if (item.getItemId() == R.id.pie_chart) {
                            popupMenu.getMenuInflater().inflate(R.menu.menu_statistical_piechart, popupMenu.getMenu());
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, pieChartFragment).commit();
                            return true;
                        } else if(item.getItemId() == R.id.amount_job){
                            popupMenu.getMenuInflater().inflate(R.menu.menu_statistical_amount_job, popupMenu.getMenu());
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, statisticalAmountJobFragment).commit();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
