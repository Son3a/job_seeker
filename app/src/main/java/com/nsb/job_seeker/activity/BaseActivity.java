package com.nsb.job_seeker.activity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.service.InternetService;

public abstract class BaseActivity extends AppCompatActivity {
    private InternetService internetService;
    private DocumentReference documentReference;
    public static boolean isConnect;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        Log.d("USERS", preferenceManager.getString(Constant.KEY_USER_ID));
//        documentReference = database.collection(Constant.KEY_COLLECTION_USERS)
//                .document(preferenceManager.getString(Constant.KEY_USER_ID));

        internetService = new InternetService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetService, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //documentReference.update(Constant.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //documentReference.update(Constant.KEY_AVAILABILITY, 1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(internetService);
    }
}
