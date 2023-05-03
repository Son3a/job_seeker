package com.nsb.job_seeker.employer;

import android.os.Bundle;
import android.view.Menu;
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
    private StatisticalAmountJobFragment statisticalAmountJobFragment;
    private PopupMenu popupMenu;

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

        showPopupMenu();


        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setPopupMenu() {
        popupMenu = new PopupMenu(StatisticalJobActivity.this, icExpend);
        popupMenu.getMenuInflater().inflate(R.menu.menu_statistical, popupMenu.getMenu());
        getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, pieChartFragment).commit();
        popupMenu.getMenu().getItem(0).setVisible(false);
    }

    private void showPopupMenu() {
        setPopupMenu();


        icExpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.pie_chart) {
                            setVisiableItemPopupMenu(0, 1, 2);
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, pieChartFragment).commit();
                            return true;
                        } else if (item.getItemId() == R.id.bar_chart) {
                            setVisiableItemPopupMenu(1, 0, 2);
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, barChartFragment).commit();
                            return true;
                        } else if (item.getItemId() == R.id.amount_job) {
                            setVisiableItemPopupMenu(2, 0, 1);
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_chart, statisticalAmountJobFragment).commit();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void setVisiableItemPopupMenu(int a, int b, int c) {
        popupMenu.getMenu().getItem(a).setVisible(false);
        popupMenu.getMenu().getItem(b).setVisible(true);
        popupMenu.getMenu().getItem(c).setVisible(true);
    }
}
