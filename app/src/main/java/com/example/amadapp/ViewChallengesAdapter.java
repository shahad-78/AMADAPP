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
import com.example.amadapp.Model.Challenge;
import com.example.amadapp.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ViewChallengesAdapter extends RecyclerView.Adapter<ViewChallengesAdapter.ViewHolder> {

    private Context context;
    private List<Challenge> challengeList;
    private List<String> challengeKeys; // Separate list for IDs
    private OnItemClickListener listener;

    // Interface now passes both the Object and the ID
    public interface OnItemClickListener {
        void onItemClick(Challenge challenge, String challengeId);
    }

    public ViewChallengesAdapter(Context context, List<Challenge> challengeList, List<String> challengeKeys, OnItemClickListener listener) {
        this.context = context;
        this.challengeList = challengeList;
        this.challengeKeys = challengeKeys;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_challange, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Challenge challenge = challengeList.get(position);
        String id = challengeKeys.get(position); // Get the ID for this position

        holder.tvTitle.setText(challenge.getTitle());
        holder.tvDesc.setText(challenge.getDescription());
        holder.chipPoints.setText(challenge.getPoints() + " Pts");

        if (challenge.getImage_url() != null && !challenge.getImage_url().isEmpty()) {
            Glide.with(context)
                    .load(challenge.getImage_url())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivImage);
        }

        // Pass both challenge and ID to the click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(challenge, id));
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDesc;
        Chip chipPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivChallengeImage);
            tvTitle = itemView.findViewById(R.id.tvChallengeTitle);
            tvDesc = itemView.findViewById(R.id.tvChallengeDesc);
            chipPoints = itemView.findViewById(R.id.chipPoints);
        }
    }
}