package com.nsb.job_seeker.activity.admin;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.navigation.NavigationBarView;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.messenger.MessageFragment;
import com.nsb.job_seeker.databinding.ActivityEmployerMainBinding;
import com.nsb.job_seeker.fragment.AccountFragment;
import com.nsb.job_seeker.fragment.admin.CompanyFragment;
import com.nsb.job_seeker.fragment.admin.RecruitmentsFragment;
import com.nsb.job_seeker.fragment.admin.CreateRecruitmentFragment;

public class EmployerMainActivity extends BaseActivity {
    private ActivityEmployerMainBinding binding;

    private RecruitmentsFragment recruitmentsFragment;
    private CreateRecruitmentFragment updateNewsFragment;
    private AccountFragment accountFragment;
    private MessageFragment messageFragment;
    private CompanyFragment companyFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityEmployerMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();
        setEvent();
    }


    private void setControl() {
        recruitmentsFragment = new RecruitmentsFragment();
        updateNewsFragment = new CreateRecruitmentFragment();
        accountFragment = new AccountFragment();
        messageFragment = new MessageFragment();
        companyFragment = new CompanyFragment();
    }

    private void setEvent() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, recruitmentsFragment)
                .addToBackStack(null)
                .commit();

        binding.bottomNavigationEmployer.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_recruitments:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, recruitmentsFragment)
                                .addToBackStack(null)
                                .commit();
                        return true;
                    case R.id.menu_up_news:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, updateNewsFragment)
                                .addToBackStack(null)
                                .commit();
                        return true;
                    case R.id.menu_company:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, companyFragment)
                                .addToBackStack(null)
                                .commit();
                        return true;
                    case R.id.menu_message:
                        getSupportFragmentManager()
                                .beginTransaction().replace(R.id.container, messageFragment)
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
    }
}
