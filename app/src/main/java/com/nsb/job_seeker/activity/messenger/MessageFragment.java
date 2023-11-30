package com.nsb.job_seeker.activity.messenger;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.adapter.RecentConversionAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomDialogDelete;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentMessageBinding;
import com.nsb.job_seeker.listener.ConversionListener;
import com.nsb.job_seeker.model.ChatMessage;
import com.nsb.job_seeker.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageFragment extends Fragment implements ConversionListener {
    private FragmentMessageBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversions;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(getLayoutInflater());

        init();
        setEvent();
        Log.d("FCM", "Token on save: " + preferenceManager.getString(Constant.KEY_FCM_TOKEN));

        return binding.getRoot();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getActivity());
        conversions = new ArrayList<>();
        conversionAdapter = new RecentConversionAdapter(conversions, this);
        binding.conversionRecycleView.setAdapter(conversionAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setEvent() {
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            getToken();
            listenConversions();
            searchAdminCompany();
        }
    }

    private void showRequireLogin() {
        CustomDialogDelete customDialogDelete = new CustomDialogDelete(getContext(), getActivity().getString(R.string.login_require), "Đăng nhập", "Để sau", R.drawable.image_login) {
            @Override
            public void doAccept() {
                gotoLogin();
            }

            @Override
            public void doCancel() {
                SeekerMainActivity.bottomNavigationView.setSelectedItemId(R.id.menu_forme);
            }
        };
        customDialogDelete.openDiaLogDelete();
    }

    private void gotoLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void searchAdminCompany() {
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        binding.textSearch.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        binding.textSearch.setMaxWidth(Integer.MAX_VALUE);

        binding.textSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                conversionAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                conversionAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void listenConversions() {
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    String receiveId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiveId;
                    if (preferenceManager.getString(Constant.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constant.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constant.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constant.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constant.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constant.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    chatMessage.conversionCompany = documentChange.getDocument().getString(Constant.KEY_COMPANY);
                    conversions.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversions.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                        if (conversions.get(i).senderId.equals(senderId) && conversions.get(i).receiverId.equals(receiverId)) {
                            conversions.get(i).message = documentChange.getDocument().getString(Constant.KEY_LAST_MESSAGE);
                            conversions.get(i).dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
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
            if (conversions.size() == 0) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
            }
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
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            Log.d("FCM", "FMC token: " + s);
            updateToken(s);
        }).addOnFailureListener(e -> {
            Log.d("FCM", "Error token: " + e.toString());
        });
    }

    private void updateToken(String token) {
        Log.d("FCM", "Token updating...");
        preferenceManager.putString(Constant.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference =
                database.collection(Constant.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constant.KEY_USER_ID)
                );
        documentReference.update(Constant.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> {
                    Log.d("Error", "Error update token: " + e.toString());
                    CustomToast.makeText(getContext(), "Unable to update token", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                });

        Log.d("FCM", "Token update done");
    }


    @Override
    public void onConversionClicked(User user, String companyName) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY_COLLECTION_USERS, user);
        intent.putExtra(Constant.KEY_COMPANY, companyName);
        startActivity(intent);
    }

    @Override
    public void onChangeListConversion(List<ChatMessage> chatMessageList) {
        if (chatMessageList.size() == 0) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("NewJob", "is pause");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (preferenceManager != null && !preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            showRequireLogin();
        }
    }

}