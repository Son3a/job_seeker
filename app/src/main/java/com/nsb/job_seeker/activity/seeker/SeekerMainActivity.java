package com.nsb.job_seeker.activity.seeker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.nsb.job_seeker.fragment.AccountFragment;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.MovableFloatingActionButton;
import com.nsb.job_seeker.activity.messenger.MessageFragment;
import com.nsb.job_seeker.fragment.seeker.MyJobFragment;
import com.nsb.job_seeker.fragment.seeker.NewsJobFragment;

public class SeekerMainActivity extends BaseActivity {
    public static BottomNavigationView bottomNavigationView;

    private NewsJobFragment newsJobFragment;
    private MyJobFragment myJobFragment;
    private MessageFragment messageFragment;
    private AccountFragment accountFragment;
    private Dialog dialogMessage;

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
        myJobFragment = new MyJobFragment();
        messageFragment = new MessageFragment();
        accountFragment = new AccountFragment();

        //setup dialog
        dialogMessage = new Dialog(this);
        dialogMessage.setContentView(R.layout.layout_message);
        dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, newsJobFragment)
                .addToBackStack(null)
                .commit();
//        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.menu_notifiation);
//        badgeDrawable.setVisible(true);
//        badgeDrawable.setNumber(10);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_forme:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, newsJobFragment)
                                .addToBackStack(null)
                                .commit();
                        return true;
                    case R.id.menu_myjob:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, myJobFragment)
                                .addToBackStack(null)
                                .commit();
                        return true;
                    case R.id.menu_message:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, messageFragment)
                                .addToBackStack(null)
                                .commit();
                        return true;
                    case R.id.menu_account:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, accountFragment)
                                .addToBackStack(null)
                                .commit();
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

    private void closeMessenger() {
//        fabMessage.setOnClickListener(v->{
//            if(dialogMessage.isShowing()){
//                dialogMessage.dismiss();
//                fabMessenger.setVisibility(View.VISIBLE);
//            }
//        });
    }
}