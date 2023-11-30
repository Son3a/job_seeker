package com.nsb.job_seeker.activity.seeker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.nsb.job_seeker.activity.BaseActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.nsb.job_seeker.adapter.SeekerMainAdapter;
import com.nsb.job_seeker.fragment.AccountFragment;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.MovableFloatingActionButton;
import com.nsb.job_seeker.activity.messenger.MessageFragment;
import com.nsb.job_seeker.fragment.seeker.MyJobFragment;
import com.nsb.job_seeker.fragment.seeker.NewsJobFragment;

public class SeekerMainActivity extends BaseActivity {
    public static BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager2;
    private SeekerMainAdapter seekerMainAdapter;

    private NewsJobFragment newsJobFragment;
    private MyJobFragment myJobFragment;
    private MessageFragment messageFragment;
    private AccountFragment accountFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_main);

        setControl();
        setEvent();
    }

    private void setControl() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager2 = findViewById(R.id.view_pager_seeker);
        seekerMainAdapter = new SeekerMainAdapter(SeekerMainActivity.this);
        viewPager2.setAdapter(seekerMainAdapter);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setOffscreenPageLimit(4);

//        newsJobFragment = new NewsJobFragment();
//        myJobFragment = new MyJobFragment();
//        messageFragment = new MessageFragment();
//        accountFragment = new AccountFragment();
//        activeFragment = new NewsJobFragment();
    }

    private void setEvent() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_forme:
                    viewPager2.setCurrentItem(0, false);
                    break;
                case R.id.menu_myjob:
                    viewPager2.setCurrentItem(1, false);
                    break;
                case R.id.menu_message:
                    viewPager2.setCurrentItem(2, false);
                    break;
                case R.id.menu_account:
                    viewPager2.setCurrentItem(3, false);
                    break;
            }
            return true;
        });

//        getSupportFragmentManager().beginTransaction().add(R.id.container, newsJobFragment, "info").commit();
//        getSupportFragmentManager().beginTransaction().add(R.id.container, myJobFragment, "info").hide(myJobFragment).commit();
//        getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment, "info").hide(messageFragment).commit();
//        getSupportFragmentManager().beginTransaction().add(R.id.container, accountFragment, "info").hide(accountFragment).commit();
//        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.menu_forme:
//                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(newsJobFragment).commit();
//                        activeFragment = newsJobFragment;
//                        return true;
//                    case R.id.menu_myjob:
//                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(myJobFragment).commit();
//                        activeFragment = myJobFragment;
//                        return true;
//                    case R.id.menu_message:
//                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(messageFragment).commit();
//                        activeFragment = messageFragment;
//                        return true;
//                    case R.id.menu_account:
//                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(accountFragment).commit();
//                        activeFragment = accountFragment;
//                        return true;
//                }
//
//                return false;
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}