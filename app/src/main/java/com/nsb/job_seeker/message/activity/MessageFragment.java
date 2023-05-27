package com.nsb.job_seeker.message.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.message.adapter.RecentConversionAdapter;
import com.nsb.job_seeker.message.listener.ConversionListener;
import com.nsb.job_seeker.message.model.ChatMessage;
import com.nsb.job_seeker.message.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MessageFragment extends Fragment implements ConversionListener {

    private View messageView;

    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversions;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;
    private RecyclerView conversionRecycleView;
    private AppCompatImageView imageSignOut;
    private RoundedImageView imageProfile;
    private TextView textName, textNotify;
    private ProgressBar progressBar;
    private FloatingActionButton fabNewChat;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        messageView = inflater.inflate(R.layout.activity_message, container, false);

        setControl();
        init();
        loadUserDetails();
        getToken();
        setEvent();
        listenConversions();

        return messageView;
    }

    private void setControl() {
        conversionRecycleView = messageView.findViewById(R.id.conversionRecycleView);
        preferenceManager = new PreferenceManager(getActivity());
        imageProfile = messageView.findViewById(R.id.image_profile);
        textName = messageView.findViewById(R.id.textName);
        progressBar = messageView.findViewById(R.id.progressBar);
        textNotify = messageView.findViewById(R.id.text_notify_empty_chats);
        fabNewChat = messageView.findViewById(R.id.fabNewChat);

        fabNewChat.setVisibility(View.GONE);
    }

    private void init() {
        conversions = new ArrayList<>();
        conversionAdapter = new RecentConversionAdapter(conversions, this);
        conversionRecycleView.setAdapter(conversionAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setEvent() {
//        imageSignOut.setOnClickListener(v -> signOut());
        fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getActivity().getApplicationContext(), UserActivity.class));
        });
    }

    private void loadUserDetails() {
        textName.setText(preferenceManager.getString(Program.KEY_NAME));
//        byte[] bytes = Base64.decode(preferenceManager.getString(Program.KEY_IMAGE), Base64.DEFAULT);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void listenConversions() {
        database.collection(Program.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Program.KEY_SENDER_ID, preferenceManager.getString(Program.KE_USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Program.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Program.KEY_RECEIVER_ID, preferenceManager.getString(Program.KE_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            textNotify.setVisibility(View.VISIBLE);
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
            conversionRecycleView.smoothScrollToPosition(0);
            conversionRecycleView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

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
        preferenceManager.putString(Program.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Program.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Program.KE_USER_ID)
                );
        documentReference.update(Program.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }


    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
        intent.putExtra(Program.KEY_USER, user);
        startActivity(intent);
    }
}