package com.nsb.job_seeker.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.databinding.ItemContainerRecentConversionBinding;
import com.nsb.job_seeker.listener.ConversionListener;
import com.nsb.job_seeker.model.ChatMessage;
import com.nsb.job_seeker.model.User;

import java.util.ArrayList;
import java.util.List;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder> implements Filterable {

    private List<ChatMessage> chatMessages;
    private List<ChatMessage> chatMessagesOld;
    private final ConversionListener conversionListener;

    public RecentConversionAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
        this.chatMessagesOld = chatMessages;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage) {
            if (chatMessage.conversionImage != null) {
                binding.imageProfile.setImageBitmap(Constant.getBitmapFromEncodedString(chatMessage.conversionImage));
            }
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.textTime.setText(" \u2022 " + Constant.getReadableDateTime(chatMessage.dateObject));
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.setId(chatMessage.conversionId);
                user.setName(chatMessage.conversionName);
                user.setImage(chatMessage.conversionImage);
                conversionListener.onConversionClicked(user, chatMessage.conversionCompany);
            });
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if (strSearch == null) {
                    chatMessages = chatMessagesOld;
                } else {
                    List<ChatMessage> list = new ArrayList<>();
                    for (ChatMessage chatMessage : chatMessagesOld) {
                        if (chatMessage.conversionName.toLowerCase().contains(strSearch.toLowerCase())) {
                            list.add(chatMessage);
                        }
                    }
                    chatMessages = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = chatMessages;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                chatMessages = (List<ChatMessage>) filterResults.values;
                conversionListener.onChangeListConversion(chatMessages);
                notifyDataSetChanged();
            }
        };
    }
}
