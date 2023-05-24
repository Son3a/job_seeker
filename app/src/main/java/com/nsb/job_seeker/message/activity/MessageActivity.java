package com.nsb.job_seeker.message.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityMainBinding;
import com.nsb.job_seeker.message.adapter.RecentConversionAdapter;
import com.nsb.job_seeker.message.listener.ConversionListener;
import com.nsb.job_seeker.message.model.ChatMessage;
import com.nsb.job_seeker.message.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversions;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(this);
        setContentView(binding.getRoot());
        init();
        loadUserDetails();
        getToken();
        setEvent();
        listenConversions();
    }

    private void init() {
        conversions = new ArrayList<>();
        conversionAdapter = new RecentConversionAdapter(conversions, this);
        binding.conversionRecycleView.setAdapter(conversionAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setEvent() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UserActivity.class));
        });
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Program.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Program.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void listenConversions(){
        database.collection(Program.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Program.KEY_SENDER_ID,preferenceManager.getString(Program.KE_USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Program.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Program.KEY_RECEIVER_ID,preferenceManager.getString(Program.KE_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Program.KEY_SENDER_ID);
                    String receiveId = documentChange.getDocument().getString(Program.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiveId;
                    if (preferenceManager.getString(Program.KE_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Program.KEU_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Program.KEU_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Program.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Program.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Program.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Program.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Program.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Program.KEY_TIMESTAMP);
                    conversions.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversions.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Program.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Program.KEY_RECEIVER_ID);
                        if (conversions.get(i).senderId.equals(senderId) && conversions.get(i).receiverId.equals(receiverId)) {
                            conversions.get(i).message = documentChange.getDocument().getString(Program.KEY_LAST_MESSAGE);
                            conversions.get(i).dateObject = documentChange.getDocument().getDate(Program.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }

            Collections.sort(conversions, (o1, o2) ->
                    new SortByDate().compare(o2, o1)
            );
            conversionAdapter.notifyDataSetChanged();
            binding.conversionRecycleView.smoothScrollToPosition(0);
            binding.conversionRecycleView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    class SortByDate implements Comparator<ChatMessage> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(ChatMessage a, ChatMessage b) {
            return a.dateObject.compareTo(b.dateObject);
        }
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Program.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Program.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Program.KE_USER_ID)
                );
        documentReference.update(Program.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Program.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Program.KE_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Program.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Program.KEY_USER,user);
        startActivity(intent);
    }
}