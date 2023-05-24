package com.nsb.job_seeker.message.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.common.PreferenceManager;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Program.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Program.KE_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Program.KEY_AVAILABILITY,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Program.KEY_AVAILABILITY,1);
    }
}
