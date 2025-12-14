package com.example.amadapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.DegradedArea;
import com.example.amadapp.Model.UsersChalleng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvNoCompleted, tvNoReports;
    private RecyclerView rvCompleted, rvReports;
    private View btnLogout, btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userID;

    private UserChallengeAdapter adapter;
    private List<UsersChalleng> completedList;

    private UserReportsAdapter reportsAdapter;
    // --- UPDATED List Type ---
    private List<UserReportsAdapter.ReportItem> reportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        // Init Views
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);

        tvNoCompleted = findViewById(R.id.tvNoCompleted);
        rvCompleted = findViewById(R.id.rvCompletedChallenges);

        tvNoReports = findViewById(R.id.tvNoReports);
        rvReports = findViewById(R.id.rvUserReports);

        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        // Setup Challenge Recycler
        completedList = new ArrayList<>();
        adapter = new UserChallengeAdapter(completedList);
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));
        rvCompleted.setAdapter(adapter);

        // Setup Reports Recycler
        reportList = new ArrayList<>();
        reportsAdapter = new UserReportsAdapter(reportList);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(reportsAdapter);

        // Listeners
        btnLogout.setOnClickListener(v -> logoutUser());
        btnBack.setOnClickListener(v -> finish());

        // Fetch Data
        fetchUserData();
        fetchCompletedChallenges();
        fetchUserReports();
    }

    private void fetchUserData() {
        mDatabase.child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String encName = snapshot.child("encryptedFullName").getValue(String.class);
                    String encEmail = snapshot.child("encryptedEmail").getValue(String.class);

                    try {
                        if (encName != null) tvName.setText(EncryptionHelper.decrypt(encName));
                        if (encEmail != null) tvEmail.setText(EncryptionHelper.decrypt(encEmail));
                    } catch (Exception e) {
                        Log.e("UserProfile", "Decryption failed", e);
                        tvName.setText("Error loading data");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCompletedChallenges() {
        mDatabase.child("usersChallenge").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                completedList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UsersChalleng challenge = data.getValue(UsersChalleng.class);
                    if (challenge != null && ("Completed".equalsIgnoreCase(challenge.getStatus())||"Rejected".equalsIgnoreCase(challenge.getStatus()))) {
                        completedList.add(challenge);
                    }
                }

                if (completedList.isEmpty()) {
                    tvNoCompleted.setVisibility(View.VISIBLE);
                    rvCompleted.setVisibility(View.GONE);
                } else {
                    tvNoCompleted.setVisibility(View.GONE);
                    rvCompleted.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserProfile", "Error fetching challenges", error.toException());
            }
        });
    }

    private void fetchUserReports() {
        mDatabase.child("DegradedAreas").orderByChild("userID").equalTo(userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reportList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            DegradedArea report = data.getValue(DegradedArea.class);
                            if (report != null) {
                                // --- UPDATED: Wrap in ReportItem to keep the ID ---
                                reportList.add(new UserReportsAdapter.ReportItem(data.getKey(), report));
                            }
                        }

                        if (reportList.isEmpty()) {
                            tvNoReports.setVisibility(View.VISIBLE);
                            rvReports.setVisibility(View.GONE);
                        } else {
                            tvNoReports.setVisibility(View.GONE);
                            rvReports.setVisibility(View.VISIBLE);
                            reportsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UserProfile", "Error fetching reports", error.toException());
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        finishAffinity();
    }
}