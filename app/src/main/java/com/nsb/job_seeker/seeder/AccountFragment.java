package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nsb.job_seeker.R;

public class AccountFragment extends Fragment {

    private Button btn;
    private View AccountView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AccountView = inflater.inflate(R.layout.fragment_seeker_account, container, false);

        setControl();
        setEvent();
        return AccountView;
    }

    private void setControl(){
        btn = AccountView.findViewById(R.id.btn_statistical);
    }

    private void setEvent(){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), StatisticalJobActivity.class);
                startActivity(i);
            }
        });
    }
}
