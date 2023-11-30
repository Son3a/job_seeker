package com.nsb.job_seeker.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.databinding.ItemFilterBinding;
import com.nsb.job_seeker.listener.FilterListener;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
    private final List<String> listFilter;
    private final FilterListener listener;
    int positionOld = 0;
    private final List<Boolean> listSelected;
    private int default_position = 0;

    public FilterAdapter(List<String> listSalary, List<Boolean> listSelected, FilterListener listener) {
        this.listFilter = listSalary;
        this.listener = listener;
        this.listSelected = listSelected;
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

        if (listSelected.get(position) == false) {
            holder.binding.imageCheck.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.imageCheck.setVisibility(View.VISIBLE);
        }
        holder.binding.getRoot().setOnClickListener(v -> {
            listener.onClickItem(listFilter.get(position), position);
            listSelected.set(default_position, false);
            listSelected.set(position, true);
            notifyItemChanged(position);
            notifyItemChanged(default_position);
            default_position = position;

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
