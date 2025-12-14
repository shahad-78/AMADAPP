package com.example.amadapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.UserTree;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserTreeAdapter extends RecyclerView.Adapter<UserTreeAdapter.TreeViewHolder> {

    private List<UserTree> userTreeList;
    private DatabaseReference treesRef;
    private Context context; // Added Context

    public UserTreeAdapter(List<UserTree> userTreeList) {
        this.userTreeList = userTreeList;
        this.treesRef = FirebaseDatabase.getInstance().getReference("Trees");
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext(); // Get Context
        View view = LayoutInflater.from(context).inflate(R.layout.item_tree_user, parent, false);
        return new TreeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder holder, int position) {
        UserTree currentUserTree = userTreeList.get(position);

        // Set Placeholders
        holder.nameTextView.setText("Loading...");
        holder.typeTextView.setText("");

        // Load Image
        if (currentUserTree.getImage() != null && !currentUserTree.getImage().isEmpty()) {
            Glide.with(context)
                    .load(currentUserTree.getImage())
                    .placeholder(R.drawable.tree_img)
                    .centerCrop()
                    .into(holder.treeImageView);
        } else {
            holder.treeImageView.setImageResource(R.drawable.tree_img);
            holder.treeImageView.setColorFilter(context.getResources().getColor(R.color.green, null));
        }

        // Fetch Name
        if (currentUserTree.getTreeID() != null) {
            treesRef.child(currentUserTree.getTreeID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String scientificName = snapshot.child("scientificName").getValue(String.class);

                        holder.nameTextView.setText(name != null ? name : "Unknown Tree");
                        holder.typeTextView.setText(currentUserTree.getDate());
                    } else {
                        holder.nameTextView.setText("Tree Info Unavailable");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.nameTextView.setText("Error");
                }
            });
        }

        // --- NEW: Handle Click Event ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserTreeDetailActivity.class);
            intent.putExtra("treeID", currentUserTree.getTreeID());
            intent.putExtra("image", currentUserTree.getImage());
            intent.putExtra("date", currentUserTree.getDate());
            intent.putExtra("lat", currentUserTree.getLat());
            intent.putExtra("lng", currentUserTree.getLng());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userTreeList.size();
    }

    public static class TreeViewHolder extends RecyclerView.ViewHolder {
        public ImageView treeImageView;
        public TextView nameTextView;
        public TextView typeTextView;

        public TreeViewHolder(@NonNull View itemView) {
            super(itemView);
            treeImageView = itemView.findViewById(R.id.tree_image);
            nameTextView = itemView.findViewById(R.id.tree_name);
            typeTextView = itemView.findViewById(R.id.tree_type);
        }
    }
}