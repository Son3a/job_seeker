package com.nsb.job_seeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.auth.Activity_ChangePassword;
import com.nsb.job_seeker.common.Activity_Profile;
import com.nsb.job_seeker.employer.StatisticalJobActivity;

public class AccountFragment extends Fragment {

    TextView tvLogin, tvLogout, tvChangePw, tvMyFile, tvDeleteAccount, tvRegister, tvStatistical;
    private View beforeLoginView, afterLoginView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        beforeLoginView = inflater.inflate(R.layout.fragment_account_before_login, container, false);
        afterLoginView = inflater.inflate(R.layout.fragment_account_after_login, container, false);

        setControl();
        setEvent();

//        if(Program.isLogin == false){
//           return beforeLoginView;
//        }
        return afterLoginView;
    }

    private void setControl() {
        tvLogin = beforeLoginView.findViewById(R.id.tv_login);
        tvRegister = beforeLoginView.findViewById(R.id.tv_register);

        tvLogout = afterLoginView.findViewById(R.id.tv_logout);
        tvChangePw = afterLoginView.findViewById(R.id.tv_change_password);
        tvMyFile = afterLoginView.findViewById(R.id.tv_my_file);
        tvDeleteAccount = afterLoginView.findViewById(R.id.tv_delete_account);
        tvStatistical = afterLoginView.findViewById(R.id.tv_statistical);
    }

    private void setEvent() {
        if(Program.role.equals("user")){
            tvStatistical.setVisibility(View.GONE);
        }

        tvStatistical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), StatisticalJobActivity.class);
                startActivity(i);
            }
        });

        tvChangePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Activity_ChangePassword.class);
                startActivity(i);
            }
        });
        tvMyFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Activity_Profile.class);
                startActivity(i);
            }
        });
    }
}
