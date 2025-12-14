package com.example.amadapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.Challenge;
import com.example.amadapp.Model.UsersChalleng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UserChallengeAdapter extends RecyclerView.Adapter<UserChallengeAdapter.ChallengeViewHolder> {

    private List<UsersChalleng> userChallengeList;
    private Context context; // Added Context

    public UserChallengeAdapter(List<UsersChalleng> userChallengeList) {
        this.userChallengeList = userChallengeList;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext(); // Get Context
        View view = LayoutInflater.from(context).inflate(R.layout.item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        UsersChalleng currentUserChallenge = userChallengeList.get(position);

        // Bind Progress
        if(currentUserChallenge.getStatus().equals("Completed")) {
            holder.progressBar.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(currentUserChallenge.getProgress());
        }

        // --- NEW: Bind Status and Set Color ---
        String status = currentUserChallenge.getStatus();
        holder.statusTextView.setText(status);

        if ("Completed".equalsIgnoreCase(status)) {
            holder.statusTextView.setTextColor(context.getResources().getColor(R.color.green));
        } else if ("Rejected".equalsIgnoreCase(status)) {
            holder.statusTextView.setTextColor(Color.RED);
        } else if ("Pending".equalsIgnoreCase(status)) {
            holder.statusTextView.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else {
            holder.statusTextView.setTextColor(Color.parseColor("#2196F3")); // Default Blue
        }

        // Placeholders
        holder.titleTextView.setText("Loading...");
        holder.descriptionTextView.setText("");
        holder.timeRemainingTextView.setText("");

        // Fetch Data
        if (currentUserChallenge.getChallengID() != null) {
            DatabaseReference challengeRef = FirebaseDatabase.getInstance().getReference("Challenges")
                    .child(currentUserChallenge.getChallengID());

            challengeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Challenge challenge = snapshot.getValue(Challenge.class);
                        if (challenge != null) {
                            holder.titleTextView.setText(challenge.getTitle());
                            holder.descriptionTextView.setText(challenge.getDescription());

                            // Calculate and set remaining time
                            String timeRemaining = calculateRemainingTime(challenge.getEnd_date());
                            holder.timeRemainingTextView.setText(timeRemaining);
                        }
                    } else {
                        holder.titleTextView.setText("Challenge Unavailable");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.titleTextView.setText("Error");
                }
            });
        }

        // --- Handle Item Click ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserChallengeDetailActivity.class);
            intent.putExtra("challengeID", currentUserChallenge.getChallengID());
            intent.putExtra("status", currentUserChallenge.getStatus());
            intent.putExtra("progress", currentUserChallenge.getProgress());
            context.startActivity(intent);
        });
    }

    // Helper method to calculate days remaining
    private String calculateRemainingTime(String endDateStr) {
        if (endDateStr == null || endDateStr.isEmpty()) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date endDate = sdf.parse(endDateStr);
            Date currentDate = new Date();

            if (endDate != null) {
                long diffInMillis = endDate.getTime() - currentDate.getTime();
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                if (diffInDays < 0) {
                    return "Ended";
                } else if (diffInDays == 0) {
                    return "Ends today";
                } else {
                    return diffInDays + " days remaining";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return userChallengeList.size();
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView timeRemainingTextView;
        public TextView statusTextView; // New Status View
        public ProgressBar progressBar;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.challenge_title);
            descriptionTextView = itemView.findViewById(R.id.challenge_description);
            timeRemainingTextView = itemView.findViewById(R.id.challenge_time_remaining);
            statusTextView = itemView.findViewById(R.id.challenge_status); // Init new view
            progressBar = itemView.findViewById(R.id.challenge_progress);
        }
    }
}