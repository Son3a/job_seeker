package com.nsb.job_seeker.employer;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.nsb.job_seeker.AccountFragment;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.message.activity.MessageFragment;

public class EmployerMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private RecruitmentsFragment recruitmentsFragment;
    private UpdateNewsFragment updateNewsFragment;
    private AccountFragment accountFragment;
    private MessageFragment messageFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_employer_main);

        setControl();
        setEvent();
    }


    private void setControl() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_employer);

        recruitmentsFragment = new RecruitmentsFragment();
        updateNewsFragment = new UpdateNewsFragment();
        accountFragment = new AccountFragment();
        messageFragment = new MessageFragment();
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, recruitmentsFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_recruitments:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, recruitmentsFragment).commit();
                        return true;
                    case R.id.menu_up_news:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, updateNewsFragment).commit();
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
    }
}
