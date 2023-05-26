package com.nsb.job_seeker.message.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityChatBinding;
import com.nsb.job_seeker.message.adapter.ChatAdapter;
import com.nsb.job_seeker.message.model.ChatMessage;
import com.nsb.job_seeker.message.model.User;
import com.nsb.job_seeker.message.network.ApiClient;
import com.nsb.job_seeker.message.network.ApiService;

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
        setEvent();
        loadReceiverDetail();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                preferenceManager.getString(Program.KE_USER_ID),
                getBitmapFromEncodedString(receiverUser.image)
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Program.KEY_SENDER_ID, preferenceManager.getString(Program.KE_USER_ID));
        message.put(Program.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Program.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Program.KEY_TIMESTAMP, new Date());
        database.collection(Program.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Program.KEY_SENDER_ID, preferenceManager.getString(Program.KE_USER_ID));
            conversion.put(Program.KEY_SENDER_NAME, preferenceManager.getString(Program.KEY_NAME));
            conversion.put(Program.KEY_SENDER_IMAGE, preferenceManager.getString(Program.KEY_IMAGE));

            conversion.put(Program.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Program.KEU_RECEIVER_NAME, receiverUser.name);
            conversion.put(Program.KEU_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Program.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Program.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Program.KE_USER_ID, preferenceManager.getString(Program.KE_USER_ID));
                data.put(Program.KEY_NAME, preferenceManager.getString(Program.KEY_NAME));
                data.put(Program.KEY_FCM_TOKEN, preferenceManager.getString(Program.KEY_FCM_TOKEN));
                data.put(Program.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Program.REMOTE_MSG_DATA, data);
                body.put(Program.REMOTE_MSG_REGISTRATION_IDS, tokens);

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
                Program.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        JSONObject reJsonObject = null;
                        try {
                            reJsonObject = new JSONObject(response.body());
                            JSONArray result = reJsonObject.getJSONArray("result");
                            if (reJsonObject.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) result.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        showToast("Notification sent successful");
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
        database.collection(Program.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Program.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Program.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Program.KEY_FCM_TOKEN);
                if(receiverUser.image == null){
                    receiverUser.image = value.getString(Program.KEY_IMAGE);
//                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }

    private void listenMessages() {
        database.collection(Program.KEY_COLLECTION_CHAT)
                .whereEqualTo(Program.KEY_SENDER_ID, preferenceManager.getString(Program.KE_USER_ID))
                .whereEqualTo(Program.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Program.KEY_COLLECTION_CHAT)
                .whereEqualTo(Program.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Program.KEY_RECEIVER_ID, preferenceManager.getString(Program.KE_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Program.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Program.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Program.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Program.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Program.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }

            Collections.sort(chatMessages, (o1, o2) ->
                    new SortByDate().compare(o1, o2)
            );

            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
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

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetail() {
        receiverUser = (User) getIntent().getSerializableExtra(Program.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void checkForConversion() {
        if (chatMessages.size() > 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Program.KE_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Program.KE_USER_ID)
            );
        }
    }

    private void setEvent() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Program.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message) {
        DocumentReference documentReference = database.collection(Program.KEY_COLLECTION_CONVERSATIONS)
                .document(conversionId);
        documentReference.update(
                Program.KEY_LAST_MESSAGE, message,
                Program.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Program.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Program.KEY_SENDER_ID, senderId)
                .whereEqualTo(Program.KEY_RECEIVER_ID, receiverId)
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