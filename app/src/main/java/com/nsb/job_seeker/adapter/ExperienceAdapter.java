package com.nsb.job_seeker.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.databinding.ItemExperienceBinding;

import java.util.List;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ExperienceHolder> {
    private final List<String> listExperiences;

    public ExperienceAdapter(List<String> listExperiences) {
        this.listExperiences = listExperiences;
    }

    @NonNull
    @Override
    public ExperienceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExperienceBinding itemExperienceBinding = ItemExperienceBinding.inflate(
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
        ItemExperienceBinding binding;

        public ExperienceHolder(ItemExperienceBinding itemExperienceBinding) {
            super(itemExperienceBinding.getRoot());
            this.binding = itemExperienceBinding;
        }

        void setData(String experience) {
            binding.textExperience.setText(experience);
        }
    }
}
