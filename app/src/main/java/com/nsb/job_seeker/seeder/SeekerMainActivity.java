package com.nsb.job_seeker.seeder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.nsb.job_seeker.AccountFragment;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.MovableFloatingActionButton;
import com.nsb.job_seeker.message.activity.MessageFragment;

public class SeekerMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private NewsJobFragment newsJobFragment;
    private SearchFragment searchFragment;
    private MyJobFragment myJobFragment;
    private MessageFragment messageFragment;
    private AccountFragment accountFragment;
    private MovableFloatingActionButton fabMessenger;
    private Dialog dialogMessage;
    private FloatingActionButton fabMessage;

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
//        fabMessenger = findViewById(R.id.fab_messenger);

        newsJobFragment = new NewsJobFragment();
        searchFragment = new SearchFragment();
        myJobFragment = new MyJobFragment();
        messageFragment = new MessageFragment();
        accountFragment = new AccountFragment();

        //setup dialog
        dialogMessage = new Dialog(this);
        dialogMessage.setContentView(R.layout.layout_message);
        dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fabMessage = dialogMessage.findViewById(R.id.fab_messenger);

        myJobFragment = new MyJobFragment();
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, newsJobFragment).commit();

//        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.menu_notifiation);
//        badgeDrawable.setVisible(true);
//        badgeDrawable.setNumber(10);

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
                    case R.id.menu_message:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, messageFragment).commit();
                        return true;
                    case R.id.menu_account:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, accountFragment).commit();
                        return true;
                }
                return false;
            }
        });

        openMessenger();
        closeMessenger();
    }

    private void openMessenger() {
//        fabMessenger.setOnClickListener(v -> {
//            fabMessenger.setVisibility(View.GONE);
//            dialogMessage.show();
//            Window window = dialogMessage.getWindow();
//            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            getSupportFragmentManager().beginTransaction().replace(R.id.layout_container_chat, new MessageFragment()).commit();

//            messageFragment.show(getSupportFragmentManager(),"MessageFragment");
//        });
    }

    private void closeMessenger(){
//        fabMessage.setOnClickListener(v->{
//            if(dialogMessage.isShowing()){
//                dialogMessage.dismiss();
//                fabMessenger.setVisibility(View.VISIBLE);
//            }
//        });
    }
}