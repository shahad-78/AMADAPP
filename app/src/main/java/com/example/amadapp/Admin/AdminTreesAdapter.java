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
import com.example.amadapp.Model.Tree;
import com.example.amadapp.R;

import java.util.List;

public class AdminTreesAdapter extends RecyclerView.Adapter<AdminTreesAdapter.ViewHolder> {

    // Helper wrapper for ID + Data
    public static class AdminTreeItem {
        public String id;
        public Tree tree;

        public AdminTreeItem(String id, Tree tree) {
            this.id = id;
            this.tree = tree;
        }
    }

    private List<AdminTreeItem> treeList;
    private OnTreeActionListener listener;

    public interface OnTreeActionListener {
        void onEdit(AdminTreeItem item);
        void onDelete(AdminTreeItem item);
    }

    public AdminTreesAdapter(List<AdminTreeItem> treeList, OnTreeActionListener listener) {
        this.treeList = treeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_tree, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminTreeItem item = treeList.get(position);
        Tree tree = item.tree;

        holder.tvName.setText(tree.getName());
        holder.tvScientific.setText(tree.getScientificName());
        holder.tvRegion.setText("Region: " + (tree.getRegion() != null ? tree.getRegion() : "N/A"));

        if (tree.getImageUrl() != null && !tree.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(tree.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.tree) // fallback if Glide fails
                    .into(holder.ivImage);
            // Remove tint if image loaded (optional, handled by placeholder logic mostly)
            holder.ivImage.clearColorFilter();
        } else {
            holder.ivImage.setImageResource(R.drawable.tree);
            holder.ivImage.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.green, null));
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return treeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvScientific, tvRegion;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivTreeImage);
            tvName = itemView.findViewById(R.id.tvTreeName);
            tvScientific = itemView.findViewById(R.id.tvScientificName);
            tvRegion = itemView.findViewById(R.id.tvRegion);
            btnEdit = itemView.findViewById(R.id.btnEditTree);
            btnDelete = itemView.findViewById(R.id.btnDeleteTree);
        }
    }
}