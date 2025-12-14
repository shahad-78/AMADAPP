package com.example.amadapp.Admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.EducationContent;
import com.example.amadapp.R;

import java.util.List;

public class AdminContentAdapter extends RecyclerView.Adapter<AdminContentAdapter.ViewHolder> {

    // Helper class to store ID with data
    public static class AdminContentItem {
        public String id;
        public EducationContent content;

        public AdminContentItem(String id, EducationContent content) {
            this.id = id;
            this.content = content;
        }
    }

    private List<AdminContentItem> contentList;
    private OnContentActionListener listener;

    public interface OnContentActionListener {
        void onEdit(AdminContentItem item);
        void onDelete(AdminContentItem item);
    }

    public AdminContentAdapter(List<AdminContentItem> contentList, OnContentActionListener listener) {
        this.contentList = contentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminContentItem item = contentList.get(position);
        EducationContent content = item.content;

        holder.tvTitle.setText(content.getTitle());
        holder.tvDate.setText(content.getDate());

        if (content.getImage() != null && !content.getImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(content.getImage())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivImage);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDate;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivContentImage);
            tvTitle = itemView.findViewById(R.id.tvContentTitle);
            tvDate = itemView.findViewById(R.id.tvContentDate);
            btnEdit = itemView.findViewById(R.id.btnEditContent);
            btnDelete = itemView.findViewById(R.id.btnDeleteContent);
        }
    }
}