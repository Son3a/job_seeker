package com.nsb.job_seeker.activity.admin;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.messenger.MessageFragment;
import com.nsb.job_seeker.adapter.EmployerMainAdapter;
import com.nsb.job_seeker.fragment.AccountFragment;
import com.nsb.job_seeker.fragment.admin.CompanyFragment;
import com.nsb.job_seeker.fragment.admin.CreateRecruitmentFragment;
import com.nsb.job_seeker.fragment.admin.RecruitmentsFragment;

public class EmployerMainActivity extends BaseActivity {
    public static BottomNavigationView bottomNavigationView;
    private EmployerMainAdapter employerMainAdapter;

    private RecruitmentsFragment recruitmentsFragment;
    private CreateRecruitmentFragment createRecruitmentFragment;
    private CompanyFragment companyFragment;
    private MessageFragment messageFragment;
    private AccountFragment accountFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_main);

        setControl();
        setEvent();
    }


    private void setControl() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_employer);

        recruitmentsFragment = new RecruitmentsFragment();
        createRecruitmentFragment = new CreateRecruitmentFragment();
        companyFragment = new CompanyFragment();
        messageFragment = new MessageFragment();
        accountFragment = new AccountFragment();
        activeFragment = new RecruitmentsFragment();
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction().add(R.id.container, recruitmentsFragment, "info").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.container, createRecruitmentFragment, "info").hide(createRecruitmentFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.container, companyFragment, "info").hide(companyFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment, "info").hide(messageFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.container, accountFragment, "info").hide(accountFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_recruitments:
                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(recruitmentsFragment).commit();
                        activeFragment = recruitmentsFragment;
                        return true;
                    case R.id.menu_up_news:
                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(createRecruitmentFragment).commit();
                        activeFragment = createRecruitmentFragment;
                        return true;
                    case R.id.menu_company:
                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(companyFragment).commit();
                        activeFragment = companyFragment;
                        return true;
                    case R.id.menu_message:
                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(messageFragment).commit();
                        activeFragment = messageFragment;
                        return true;
                    case R.id.menu_account:
                        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(accountFragment).commit();
                        activeFragment = accountFragment;
                        return true;
                }
                return false;
            }
        });
    }
}
