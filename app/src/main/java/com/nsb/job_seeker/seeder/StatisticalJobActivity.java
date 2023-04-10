package com.nsb.job_seeker.seeder;

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

public class StatisticalJobActivity extends AppCompatActivity {
    private ImageView icExpend, icBack;
    private BarChartFragment barChartFragment;
    private PieChartFragment pieChartFragment;

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
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, pieChartFragment).commit();

        icExpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragmentInFrame = getSupportFragmentManager().findFragmentById(R.id.container_chart);
                PopupMenu popupMenu = new PopupMenu(StatisticalJobActivity.this, v);
                Fragment temp;
                if (fragmentInFrame instanceof PieChartFragment) {
                    popupMenu.getMenuInflater().inflate(R.menu.menu_statistival_barchart, popupMenu.getMenu());
                    temp = barChartFragment;
                } else {
                    popupMenu.getMenuInflater().inflate(R.menu.menu_statistival_piechart, popupMenu.getMenu());
                    temp = pieChartFragment;
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, temp).commit();
                        return true;
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
