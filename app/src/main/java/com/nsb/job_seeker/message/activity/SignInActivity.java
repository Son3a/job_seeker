package com.nsb.job_seeker.message.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.getBoolean(Program.KEY_IS_SIGNED_IN)) {
            startActivity(new Intent(getApplicationContext(), MessageActivity.class));
            finish();
        }
        setContentView(binding.getRoot());

        setEvent();
    }

    private void setEvent() {
        binding.textCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });

        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
    }

    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Program.KEY_COLLECTION_USERS)
                .whereEqualTo(Program.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Program.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null &&
                                    task.getResult().getDocuments().size() > 0) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                preferenceManager.putBoolean(Program.KEY_IS_SIGNED_IN, true);
                                preferenceManager.putString(Program.KE_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Program.KEY_NAME, documentSnapshot.getString(Program.KEY_NAME));
                                preferenceManager.putString(Program.KEY_IMAGE, documentSnapshot.getString(Program.KEY_IMAGE));
                                Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                loading(false);
                                showToast("Email or Password are not correct");
                            }
                        }
                );
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }
        return true;
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}