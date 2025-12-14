package com.example.amadapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.EducationContent;
import com.example.amadapp.R;

import java.util.List;

public class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.ViewHolder> {

    private Context context;
    private List<EducationContent> resourceList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EducationContent content);
    }

    public ResourcesAdapter(Context context, List<EducationContent> resourceList, OnItemClickListener listener) {
        this.context = context;
        this.resourceList = resourceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EducationContent content = resourceList.get(position);

        holder.tvTitle.setText(content.getTitle());
        holder.tvDate.setText(content.getDate());

        // Load image using Glide
        if (content.getImage() != null && !content.getImage().isEmpty()) {
            Glide.with(context)
                    .load(content.getImage())
                    .placeholder(android.R.drawable.ic_menu_gallery) // Default placeholder
                    .error(android.R.drawable.ic_dialog_alert) // Error image
                    .into(holder.ivImage);
        }

        // Handle Click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(content));
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivResourceImage);
            tvTitle = itemView.findViewById(R.id.tvResourceTitle);
            tvDate = itemView.findViewById(R.id.tvResourceDate);
        }
    }
}