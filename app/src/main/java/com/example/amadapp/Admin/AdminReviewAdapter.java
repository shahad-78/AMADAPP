package com.example.amadapp.Admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.Challenge;
import com.example.amadapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {

    public static class ReviewItem {
        public String userId;
        public String pushId;
        public String challengeId;
        public String proofUrl;
        public String status;

        public ReviewItem(String userId, String pushId, String challengeId, String proofUrl, String status) {
            this.userId = userId;
            this.pushId = pushId;
            this.challengeId = challengeId;
            this.proofUrl = proofUrl;
            this.status = status;
        }
    }

    private List<ReviewItem> reviewList;
    private Context context;

    public AdminReviewAdapter(List<ReviewItem> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_review_submission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewItem item = reviewList.get(position);

        holder.tvUserId.setText("User ID: " + item.userId);
        holder.tvStatus.setText(item.status);

        // Fetch Challenge Name dynamically
        DatabaseReference challengeRef = FirebaseDatabase.getInstance().getReference("Challenges").child(item.challengeId);
        challengeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    holder.tvChallengeName.setText(title != null ? title : "Unknown Challenge");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Click listener to open detail activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminReviewDetailActivity.class);
            intent.putExtra("userId", item.userId);
            intent.putExtra("pushId", item.pushId);
            intent.putExtra("challengeId", item.challengeId);
            intent.putExtra("proofUrl", item.proofUrl);
            intent.putExtra("status", item.status);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvChallengeName, tvUserId, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChallengeName = itemView.findViewById(R.id.tvChallengeName);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}