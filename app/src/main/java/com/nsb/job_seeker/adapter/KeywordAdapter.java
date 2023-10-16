package com.nsb.job_seeker.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.databinding.ItemSeachKeywordBinding;
import com.nsb.job_seeker.room.KeyWord;

import java.util.List;

public class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.KeywordHolder> {
    private final List<KeyWord> keyWordList;

    public KeywordAdapter(List<KeyWord> keyWordList) {
        this.keyWordList = keyWordList;
    }

    @NonNull
    @Override
    public KeywordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSeachKeywordBinding itemSeachKeywordBinding = ItemSeachKeywordBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new KeywordHolder(itemSeachKeywordBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordHolder holder, int position) {
        holder.setData(keyWordList.get(position));
    }

    @Override
    public int getItemCount() {
        return keyWordList.size();
    }

    class KeywordHolder extends RecyclerView.ViewHolder {
        ItemSeachKeywordBinding binding;

        public KeywordHolder(ItemSeachKeywordBinding itemSeachKeywordBinding) {
            super(itemSeachKeywordBinding.getRoot());
            this.binding = itemSeachKeywordBinding;
        }

        void setData(KeyWord keyWord) {
            binding.textKeyWord.setText(keyWord.getName());
            Log.d("KeyWord", keyWord.getName());
        }
    }
}
