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
import com.example.amadapp.Model.Tree;
import com.example.amadapp.R;

import java.util.List;

public class TreeRecommendationsAdapter extends RecyclerView.Adapter<TreeRecommendationsAdapter.ViewHolder> {

    private Context context;
    private List<Tree> treeList;
    private List<String> treeKeys; // Store Keys
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Tree tree, String treeID);
    }

    // Constructor updated to accept keys and listener
    public TreeRecommendationsAdapter(Context context, List<Tree> treeList, List<String> treeKeys, OnItemClickListener listener) {
        this.context = context;
        this.treeList = treeList;
        this.treeKeys = treeKeys;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trees, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tree tree = treeList.get(position);
        String key = treeKeys.get(position);

        holder.tvName.setText(tree.getName());
        holder.tvScientific.setText(tree.getScientificName());
        holder.tvRegion.setText("Suitable for: " + tree.getRegion());
        holder.tvDesc.setText(tree.getDescription());

        if (tree.getImageUrl() != null && !tree.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(tree.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivImage);
        }

        // Set Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(tree, key);
            }
        });
    }

    @Override
    public int getItemCount() {
        return treeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvScientific, tvRegion, tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivTreeImage);
            tvName = itemView.findViewById(R.id.tvTreeName);
            tvScientific = itemView.findViewById(R.id.tvScientificName);
            tvRegion = itemView.findViewById(R.id.tvRegion);
            tvDesc = itemView.findViewById(R.id.tvDescription);
        }
    }
}