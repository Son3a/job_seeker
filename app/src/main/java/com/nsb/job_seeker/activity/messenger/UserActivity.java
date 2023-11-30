package com.nsb.job_seeker.activity.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityUserActivityBinding;
import com.nsb.job_seeker.adapter.UserAdapter;
import com.nsb.job_seeker.listener.UserListener;
import com.nsb.job_seeker.model.User;

import java.util.ArrayList;
import java.util.List;

public class        UserActivity extends BaseActivity implements UserListener {

    private ActivityUserActivityBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        setEvent();
    }

    private void setEvent() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection((Constant.KEY_COLLECTION_USERS))
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currenUserId = preferenceManager.getString(Constant.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currenUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.setName(queryDocumentSnapshot.getString(Constant.KEY_NAME));
                            user.setEmail(queryDocumentSnapshot.getString(Constant.KEY_EMAIL));
                            user.setImage(queryDocumentSnapshot.getString(Constant.KEY_IMAGE));
                            user.setToken(queryDocumentSnapshot.getString(Constant.KEY_FCM_TOKEN));
                            user.setId(queryDocumentSnapshot.getId());
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(users,this);
                            binding.userRecycleView.setAdapter(userAdapter);
                            binding.userRecycleView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText((String.format("%s", "No user available")));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserClicked(User user){
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constant.KEY_COLLECTION_USERS, user);
        startActivity(intent);
        finish();
    }
}