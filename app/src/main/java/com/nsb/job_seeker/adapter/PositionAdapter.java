package com.nsb.job_seeker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsb.job_seeker.databinding.ItemFilterBinding;
import com.nsb.job_seeker.listener.PositionListener;

import java.util.ArrayList;
import java.util.List;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ExperienceHolder> implements Filterable {
    private List<String> listPosition;
    private final PositionListener listener;
    private List<String> listPositionOld;
    private final List<Boolean> listSelected;
    private int default_position = 0;
    public PositionAdapter(List<String> listPosition, PositionListener listener, List<Boolean> listSelected) {
        this.listPosition = listPosition;
        this.listener = listener;
        this.listPositionOld = listPosition;
        this.listSelected = listSelected;
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
        holder.setData(listPosition.get(position));

        if (listSelected.get(position) == false) {
            holder.binding.imageCheck.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.imageCheck.setVisibility(View.VISIBLE);
        }
        holder.binding.getRoot().setOnClickListener(v -> {
            listener.onClickPosition(listPosition.get(position));
            listSelected.set(default_position, false);
            listSelected.set(position, true);
            notifyItemChanged(position);
            notifyItemChanged(default_position);
            default_position = position;

        });
    }

    @Override
    public int getItemCount() {
        return listPosition.size();
    }

    class ExperienceHolder extends RecyclerView.ViewHolder {
        ItemFilterBinding binding;

        public ExperienceHolder(ItemFilterBinding itemExperienceBinding) {
            super(itemExperienceBinding.getRoot());
            this.binding = itemExperienceBinding;
        }

        void setData(String data) {
            binding.textExperience.setText(data);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if(strSearch == null){
                    listPosition = listPositionOld;
                } else {
                    List<String> list = new ArrayList<>();
                    for(String str: listPositionOld){
                        if(str.toLowerCase().contains(strSearch.toLowerCase())){
                            list.add(str);
                        }
                    }
                    listPosition = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = listPosition;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listPosition = (List<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
