package com.nsb.job_seeker.seeder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.nsb.job_seeker.AccountFragment;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.MovableFloatingActionButton;

public class SeekerMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private NewsJobFragment newsJobFragment;
    private SearchFragment searchFragment;
    private MyJobFragment myJobFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    private MovableFloatingActionButton fabMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_seeker_main);

        setControl();
        setEvent();
    }

    private void setControl() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabMessenger = findViewById(R.id.fab_messenger);

        newsJobFragment = new NewsJobFragment();
        searchFragment = new SearchFragment();
        myJobFragment = new MyJobFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, newsJobFragment).commit();

        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.menu_notifiation);
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(10);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_forme:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, newsJobFragment).commit();
                        return true;
                    case R.id.menu_search:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFragment).commit();
                        return true;
                    case R.id.menu_myjob:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, myJobFragment).commit();
                        return true;
                    case R.id.menu_notifiation:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, notificationFragment).commit();
                        return true;
                    case R.id.menu_account:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, accountFragment).commit();
                        return true;
                }
                return false;
            }
        });

        openMessenger();
    }

    private void openMessenger() {
        fabMessenger.setOnClickListener(v ->{
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            fabMessenger.animate()
                    .x(width-150)
                    .y(30)
                    .setDuration(500);
        });
    }
}