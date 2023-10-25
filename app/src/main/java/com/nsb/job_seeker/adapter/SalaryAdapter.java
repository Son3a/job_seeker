package com.nsb.job_seeker.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.databinding.ItemFilterBinding;
import com.nsb.job_seeker.listener.SalaryListener;

import java.util.List;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.ExperienceHolder> {
    private final List<String> listExperiences;
    private final SalaryListener listener;

    public SalaryAdapter(List<String> listExperiences, SalaryListener listener) {
        this.listExperiences = listExperiences;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExperienceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterBinding itemExperienceBinding = ItemFilterBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ExperienceHolder(itemExperienceBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceHolder holder, int position) {
        holder.setData(listExperiences.get(position));
    }

    @Override
    public int getItemCount() {
        return listExperiences.size();
    }

    class ExperienceHolder extends RecyclerView.ViewHolder {
        ItemFilterBinding binding;

        public ExperienceHolder(ItemFilterBinding itemExperienceBinding) {
            super(itemExperienceBinding.getRoot());
            this.binding = itemExperienceBinding;
        }

        void setData(String data) {
            binding.textExperience.setText(data);
            binding.getRoot().setOnClickListener(v -> {
                listener.onClickSalary(data);
            });
        }
    }
}
