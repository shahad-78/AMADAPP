package com.example.amadapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.amadapp.Model.UsersChalleng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChallengeDetailActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvTitle, tvDesc, tvDate, tvPoints;
    private Button btnJoin;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String challengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_challenge_detail);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Init Views
        ivImage = findViewById(R.id.ivDetailImage);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvDate = findViewById(R.id.tvDetailDate);
        tvPoints = findViewById(R.id.tvDetailPoints);
        btnJoin = findViewById(R.id.btnJoinChallenge);

        // Get Data
        challengeId = getIntent().getStringExtra("id"); // Retrieve ID
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("desc");
        String startDate = getIntent().getStringExtra("start_date");
        String endDate = getIntent().getStringExtra("end_date");
        int points = getIntent().getIntExtra("points", 0);
        String imageUrl = getIntent().getStringExtra("image_url");

        // Set Data
        tvTitle.setText(title);
        tvDesc.setText(desc);
        tvPoints.setText(points + " Points");
        tvDate.setText("Start: " + startDate + "  |  End: " + endDate);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(ivImage);
        }

        // Check user status
        if (mAuth.getCurrentUser() == null) {
            btnJoin.setEnabled(false);
            btnJoin.setText("Login to Join");
        } else {
            // --- NEW: Check if user already joined on load ---
            checkIfAlreadyJoined();
        }

        // Join Button Click Listener
        btnJoin.setOnClickListener(v -> joinChallenge());
    }

    // --- NEW: Helper method to check join status ---
    private void checkIfAlreadyJoined() {
        if (mAuth.getCurrentUser() != null && challengeId != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Query: usersChallenge -> userID -> where challengID == currentChallengeId
            mDatabase.child("usersChallenge")
                    .child(userId)
                    .orderByChild("challengID") // Must match field name in UsersChalleng model
                    .equalTo(challengeId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // User has found a match, so they joined already
                                updateUIJoined();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error silently
                        }
                    });
        }
    }

    private void updateUIJoined() {
        btnJoin.setText("Joined");
        btnJoin.setEnabled(false);
        btnJoin.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void joinChallenge() {
        if (mAuth.getCurrentUser() != null && challengeId != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Disable button immediately to prevent double clicks
            btnJoin.setEnabled(false);

            // --- Double Check before writing to DB ---
            mDatabase.child("usersChallenge")
                    .child(userId)
                    .orderByChild("challengID")
                    .equalTo(challengeId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Already joined
                                Toast.makeText(ChallengeDetailActivity.this, "You have already joined this challenge!", Toast.LENGTH_SHORT).show();
                                updateUIJoined();
                            } else {
                                // Not joined yet, proceed to save
                                performJoin(userId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            btnJoin.setEnabled(true);
                            Toast.makeText(ChallengeDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Error: User or Challenge ID missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void performJoin(String userId) {
        // Create the object based on your UsersChalleng model
        UsersChalleng userChallenge = new UsersChalleng(challengeId, "Joined","" ,0);

        mDatabase.child("usersChallenge")
                .child(userId)
                .push()
                .setValue(userChallenge)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChallengeDetailActivity.this, "Successfully Joined Challenge!", Toast.LENGTH_SHORT).show();
                    updateUIJoined();
                })
                .addOnFailureListener(e -> {
                    btnJoin.setEnabled(true); // Re-enable on failure
                    Toast.makeText(ChallengeDetailActivity.this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}