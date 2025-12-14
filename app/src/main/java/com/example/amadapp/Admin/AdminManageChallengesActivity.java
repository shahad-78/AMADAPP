package com.example.amadapp.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.Challenge;
import com.example.amadapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageChallengesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private AdminChallengesAdapter adapter;
    private List<AdminChallengesAdapter.AdminChallengeItem> challengeList;
    private DatabaseReference mDatabase;

    // UI Stats & Buttons
    private TextView tvTotalChallenges, tvPendingSubmissions;
    private Button btnReviewSubmissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_challenges);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Challenges");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Init UI
        tvTotalChallenges = findViewById(R.id.tvTotalChallenges);
        tvPendingSubmissions = findViewById(R.id.tvPendingSubmissions);
        btnReviewSubmissions = findViewById(R.id.btnReviewSubmissions);
        recyclerView = findViewById(R.id.rvChallenges);
        fabAdd = findViewById(R.id.fabAddChallenge);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        challengeList = new ArrayList<>();

        adapter = new AdminChallengesAdapter(challengeList, new AdminChallengesAdapter.OnChallengeActionListener() {
            @Override
            public void onEdit(AdminChallengesAdapter.AdminChallengeItem item) {
                openAddEditActivity(item);
            }

            @Override
            public void onDelete(AdminChallengesAdapter.AdminChallengeItem item) {
                confirmDelete(item);
            }
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> openAddEditActivity(null));

        // New Button Listener
        btnReviewSubmissions.setOnClickListener(v -> {
            // Navigate to Review Activity
            // Note: You need to create AdminReviewSubmissionsActivity
            Intent intent = new Intent(AdminManageChallengesActivity.this, AdminReviewSubmissionsActivity.class);
            startActivity(intent);
        });

        fetchChallenges();
        fetchStats();
    }

    private void fetchChallenges() {
        mDatabase.child("Challenges").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                challengeList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Challenge challenge = data.getValue(Challenge.class);
                    if (challenge != null) {
                        challengeList.add(new AdminChallengesAdapter.AdminChallengeItem(data.getKey(), challenge));
                    }
                }
                adapter.notifyDataSetChanged();

                // Update Total Count UI locally or rely on fetchStats
                tvTotalChallenges.setText(String.valueOf(challengeList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminManageChallengesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStats() {
        // Count Pending User Challenges
        mDatabase.child("usersChallenge").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long pendingCount = 0;
                // usersChallenge -> userId -> pushId -> { status: "Pending" }
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot challengeSnapshot : userSnapshot.getChildren()) {
                        String status = challengeSnapshot.child("status").getValue(String.class);
                        if ("Pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }
                    }
                }
                tvPendingSubmissions.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void openAddEditActivity(AdminChallengesAdapter.AdminChallengeItem item) {
        Intent intent = new Intent(this, AdminAddChallengeActivity.class);
        if (item != null) {
            intent.putExtra("id", item.id);
            intent.putExtra("title", item.challenge.getTitle());
            intent.putExtra("description", item.challenge.getDescription());
            intent.putExtra("start_date", item.challenge.getStart_date());
            intent.putExtra("end_date", item.challenge.getEnd_date());
            intent.putExtra("points", item.challenge.getPoints());
            intent.putExtra("image_url", item.challenge.getImage_url());
        }
        startActivity(intent);
    }

    private void confirmDelete(AdminChallengesAdapter.AdminChallengeItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Challenge")
                .setMessage("Delete " + item.challenge.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDatabase.child("Challenges").child(item.id).removeValue();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}