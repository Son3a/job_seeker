package com.nsb.job_seeker.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.ItemContainerReceivedMessageBinding;
import com.nsb.job_seeker.databinding.ItemContainerSentMessageBinding;
import com.nsb.job_seeker.model.ChatMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private String receiverProfileImage;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, String receiverProfileImage) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.receiverProfileImage = receiverProfileImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));

            if (position == 0) {
                String timeConversation = new SimpleDateFormat("dd MMMM, HH:mm", Locale.getDefault()).format(chatMessages.get(position).dateObject);
                ((SentMessageViewHolder) holder).binding.textDateConversation.setText(timeConversation);
                ((SentMessageViewHolder) holder).binding.textDateConversation.setVisibility(View.VISIBLE);
            }
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
            if (position == 0) {
                String timeConversation = new SimpleDateFormat("dd MMMM, HH:mm", Locale.getDefault()).format(chatMessages.get(position).dateObject);
                ((ReceivedMessageViewHolder) holder).binding.textDateConversation.setText(timeConversation);
                ((ReceivedMessageViewHolder) holder).binding.textDateConversation.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.textMessage.setOnClickListener(v -> {
                if (binding.textDateTime.getVisibility() == View.GONE) {
                    binding.textDateTime.setVisibility(View.VISIBLE);
                } else {
                    binding.textDateTime.setVisibility(View.GONE);
                }
            });
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, String receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

            if(URLUtil.isValidUrl(receiverProfileImage)){
                Picasso.get().load(receiverProfileImage).into(binding.imageProfile);
            } else {
                binding.imageProfile.setImageBitmap(Constant.getBitmapFromEncodedString(receiverProfileImage));
            }
            binding.textMessage.setOnClickListener(v -> {
                if (binding.textDateTime.getVisibility() == View.GONE) {
                    binding.textDateTime.setVisibility(View.VISIBLE);
                } else {
                    binding.textDateTime.setVisibility(View.GONE);
                }
            });
        }
    }
}
