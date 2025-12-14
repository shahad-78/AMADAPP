package com.example.amadapp.Admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.amadapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminReviewDetailActivity extends AppCompatActivity {

    private ImageView ivProof;
    private TextView tvTitle, tvUser, tvStatus;
    private Button btnConfirm, btnReject;

    private String userId, pushId, challengeId, proofUrl, status;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_review_detail);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivProof = findViewById(R.id.ivDetailProof);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvUser = findViewById(R.id.tvDetailUser);
        tvStatus = findViewById(R.id.tvDetailStatus);
        btnConfirm = findViewById(R.id.btnDetailConfirm);
        btnReject = findViewById(R.id.btnDetailReject);

        // Get Intent Data
        userId = getIntent().getStringExtra("userId");
        pushId = getIntent().getStringExtra("pushId");
        challengeId = getIntent().getStringExtra("challengeId");
        proofUrl = getIntent().getStringExtra("proofUrl");
        status = getIntent().getStringExtra("status");

        // Set UI
        tvUser.setText("User ID: " + userId);
        tvStatus.setText("Status: " + status);

        // --- NEW: Check status to disable buttons ---
        if ("Completed".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
            btnConfirm.setEnabled(false);
            btnReject.setEnabled(false);

            // Optional: Visually dim the buttons
            btnConfirm.setAlpha(0.5f);
            btnReject.setAlpha(0.5f);
        }

        if (proofUrl != null && !proofUrl.isEmpty()) {
            Glide.with(this).load(proofUrl).into(ivProof);
        }

        // Fetch Challenge Title
        if (challengeId != null) {
            mDatabase.child("Challenges").child(challengeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String title = snapshot.child("title").getValue(String.class);
                        tvTitle.setText(title);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        btnConfirm.setOnClickListener(v -> updateStatus("Completed"));
        btnReject.setOnClickListener(v -> updateStatus("Rejected"));
    }

    private void updateStatus(String newStatus) {
        if (userId == null || pushId == null) return;

        mDatabase.child("usersChallenge")
                .child(userId)
                .child(pushId)
                .child("status")
                .setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Marked as " + newStatus, Toast.LENGTH_SHORT).show();

                    // Disable buttons immediately after action
                    btnConfirm.setEnabled(false);
                    btnReject.setEnabled(false);
                    btnConfirm.setAlpha(0.5f);
                    btnReject.setAlpha(0.5f);

                    tvStatus.setText("Status: " + newStatus);
                    // Optionally finish to go back
                    // finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }
}