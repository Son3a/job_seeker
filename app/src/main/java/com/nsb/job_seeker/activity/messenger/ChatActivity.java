package com.nsb.job_seeker.activity.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityChatBinding;
import com.nsb.job_seeker.adapter.ChatAdapter;
import com.nsb.job_seeker.model.ChatMessage;
import com.nsb.job_seeker.model.User;
import com.nsb.job_seeker.network.ApiClient;
import com.nsb.job_seeker.network.ApiService;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        init();
        setEvent();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        chatMessages = new ArrayList<>();

        loadReceiverDetail();

        if (receiverUser != null) {
            chatAdapter = new ChatAdapter(
                    chatMessages,
                    preferenceManager.getString(Constant.KEY_USER_ID),
                    receiverUser.getImage()
            );
            binding.chatRecycleView.setAdapter(chatAdapter);
        }
    }

    private void setEvent() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        hideKeyBoard();
    }

    private void hideKeyBoard() {
        binding.chatRecycleView.setOnClickListener(v -> {
            Constant.hideKeyboardFrom(getApplicationContext(), binding.chatRecycleView);
        });
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID));
        message.put(Constant.KEY_RECEIVER_ID, receiverUser.getId());
        message.put(Constant.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constant.KEY_TIMESTAMP, new Date());
        database.collection(Constant.KEY_COLLECTION_CHAT).add(message).addOnSuccessListener(runnable -> {
            binding.textNotifyEmptyChats.setVisibility(View.GONE);
        });
        if (conversionId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID));
            conversion.put(Constant.KEY_SENDER_NAME, preferenceManager.getString(Constant.KEY_NAME));
            conversion.put(Constant.KEY_SENDER_IMAGE, preferenceManager.getString(Constant.AVATAR));

            conversion.put(Constant.KEY_RECEIVER_ID, receiverUser.getId());
            conversion.put(Constant.KEY_RECEIVER_NAME, receiverUser.getName());
            conversion.put(Constant.KEY_RECEIVER_IMAGE, receiverUser.getImage());
            conversion.put(Constant.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constant.KEY_COMPANY, getIntent().getStringExtra(Constant.KEY_COMPANY));
            conversion.put(Constant.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable && receiverUser.getToken() != null) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.getToken());

                JSONObject data = new JSONObject();
                data.put(Constant.KEY_USER_ID, preferenceManager.getString(Constant.KEY_USER_ID));
                data.put(Constant.KEY_NAME, preferenceManager.getString(Constant.KEY_NAME));
                data.put(Constant.KEY_FCM_TOKEN, preferenceManager.getString(Constant.KEY_FCM_TOKEN));
                data.put(Constant.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constant.REMOTE_MSG_DATA, data);
                body.put(Constant.REMOTE_MSG_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception e) {
                showToast(e.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constant.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        JSONObject reJsonObject = null;
                        try {
                            reJsonObject = new JSONObject(response.body());
                            Log.d("ResultBody", reJsonObject.toString());
                            JSONArray result = reJsonObject.getJSONArray("results");
                            if (reJsonObject.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) result.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        } catch (JSONException e) {
                            Log.d("Error", "Err chat: " + e.toString());
                            e.printStackTrace();
                        }
                    }
                } else {
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        if (receiverUser == null) return;

        database.collection(Constant.KEY_COLLECTION_USERS).document(
                receiverUser.getId()
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constant.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constant.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.setToken(value.getString(Constant.KEY_FCM_TOKEN));
                if (receiverUser.getImage() == null) {
                    receiverUser.setImage(value.getString(Constant.KEY_IMAGE));
//                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setText("Online");
                binding.imageStatus.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setText("Offline");
                binding.imageStatus.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void listenMessages() {
        database.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);

        database.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(Constant.KEY_RECEIVER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            binding.textNotifyEmptyChats.setVisibility(View.VISIBLE);
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constant.KEY_MESSAGE);
                    chatMessage.dateTime = Constant.getReadableDateTime(documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }

            if (chatMessages.size() == 0) {
                chatAdapter.notifyDataSetChanged();
                binding.textNotifyEmptyChats.setVisibility(View.VISIBLE);
            } else {
                binding.textNotifyEmptyChats.setVisibility(View.GONE);
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    });

    class SortByDate implements Comparator<ChatMessage> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(ChatMessage a, ChatMessage b) {
            return a.dateObject.compareTo(b.dateObject);
        }
    }

    private void loadReceiverDetail() {
        receiverUser = (User) getIntent().getSerializableExtra(Constant.KEY_COLLECTION_USERS);
        binding.textName.setText(receiverUser.getName());
        if (receiverUser.getImage() != null) {
            if (URLUtil.isValidUrl(receiverUser.getImage())) {
                Picasso.get().load(receiverUser.getImage()).into(binding.imageAvatarReceiver);
            } else {
                binding.imageAvatarReceiver.setImageBitmap(Constant.getBitmapFromEncodedString(receiverUser.getImage()));
            }
        }
        if (preferenceManager.getString(Constant.ROLE).equals(Constant.USER_ROLE)) {
            binding.textNameCompany.setText(getIntent().getStringExtra(Constant.KEY_COMPANY));
            binding.textNameCompany.setVisibility(View.VISIBLE);
        } else {
            binding.textNameCompany.setVisibility(View.GONE);
        }
    }

    private void checkForConversion() {
        if (chatMessages.size() > 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constant.KEY_USER_ID),
                    receiverUser.getId()
            );
            checkForConversionRemotely(
                    receiverUser.getId(),
                    preferenceManager.getString(Constant.KEY_USER_ID)
            );
        }
    }


    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message) {
        DocumentReference documentReference = database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .document(conversionId);
        documentReference.update(
                Constant.KEY_LAST_MESSAGE, message,
                Constant.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}