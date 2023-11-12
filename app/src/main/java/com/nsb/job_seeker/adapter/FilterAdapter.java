package com.nsb.job_seeker.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.databinding.ItemFilterBinding;
import com.nsb.job_seeker.listener.FilterListener;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
    private final List<String> listFilter;
    private final FilterListener listener;

    public FilterAdapter(List<String> listSalary, FilterListener listener) {
        this.listFilter = listSalary;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterBinding itemFilterBinding = ItemFilterBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FilterHolder(itemFilterBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterHolder holder, int position) {
        holder.setData(listFilter.get(position));

        holder.binding.getRoot().setOnClickListener(v -> {
            listener.onClickItem(listFilter.get(position), position);
        });
    }

    @Override
    public int getItemCount() {
        return listFilter.size();
    }

    class FilterHolder extends RecyclerView.ViewHolder {
        ItemFilterBinding binding;

        public FilterHolder(ItemFilterBinding itemExperienceBinding) {
            super(itemExperienceBinding.getRoot());
            this.binding = itemExperienceBinding;
        }

        void setData(String data) {
            binding.textExperience.setText(data);
        }
    }
}
