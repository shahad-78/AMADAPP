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
import com.example.amadapp.Model.Challenge;
import com.example.amadapp.R;

import java.util.List;

public class AdminChallengesAdapter extends RecyclerView.Adapter<AdminChallengesAdapter.ViewHolder> {

    // Helper wrapper for ID + Data
    public static class AdminChallengeItem {
        public String id;
        public Challenge challenge;

        public AdminChallengeItem(String id, Challenge challenge) {
            this.id = id;
            this.challenge = challenge;
        }
    }

    private List<AdminChallengeItem> challengeList;
    private OnChallengeActionListener listener;

    public interface OnChallengeActionListener {
        void onEdit(AdminChallengeItem item);
        void onDelete(AdminChallengeItem item);
    }

    public AdminChallengesAdapter(List<AdminChallengeItem> challengeList, OnChallengeActionListener listener) {
        this.challengeList = challengeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminChallengeItem item = challengeList.get(position);
        Challenge challenge = item.challenge;

        holder.tvTitle.setText(challenge.getTitle());
        holder.tvPoints.setText(challenge.getPoints() + " Pts");
        holder.tvDates.setText(challenge.getStart_date() + " - " + challenge.getEnd_date());

        if (challenge.getImage_url() != null && !challenge.getImage_url().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(challenge.getImage_url())
                    .centerCrop()
                    .placeholder(R.drawable.trophy)
                    .into(holder.ivImage);
            holder.ivImage.clearColorFilter();
        } else {
            holder.ivImage.setImageResource(R.drawable.trophy);
            holder.ivImage.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_light, null)); // Simple tint fallback
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvPoints, tvDates;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivChallengeImage);
            tvTitle = itemView.findViewById(R.id.tvChallengeTitle);
            tvPoints = itemView.findViewById(R.id.tvChallengePoints);
            tvDates = itemView.findViewById(R.id.tvChallengeDates);
            btnEdit = itemView.findViewById(R.id.btnEditChallenge);
            btnDelete = itemView.findViewById(R.id.btnDeleteChallenge);
        }
    }
}