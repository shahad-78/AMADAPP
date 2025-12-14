package com.example.amadapp.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminReviewSubmissionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ChipGroup chipGroupFilter;

    // Stats Views
    private TextView tvPending, tvCompleted, tvRejected;

    private AdminReviewAdapter adapter;
    private List<AdminReviewAdapter.ReviewItem> allReviews; // Stores everything fetched
    private List<AdminReviewAdapter.ReviewItem> filteredList; // Stores what is currently shown
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_review_submissions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Review Submissions");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = findViewById(R.id.rvReviews);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        // Init Stats Views
        tvPending = findViewById(R.id.tvCountPending);
        tvCompleted = findViewById(R.id.tvCountCompleted);
        tvRejected = findViewById(R.id.tvCountRejected);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allReviews = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Use the updated adapter (without interface, as it handles clicks internally)
        adapter = new AdminReviewAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // Filter Listener
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            String status = "Pending"; // Default
            if (checkedId == R.id.chipCompleted) status = "Completed";
            else if (checkedId == R.id.chipRejected) status = "Rejected";

            filterData(status);
        });

        fetchAllReviews();
    }

    private void fetchAllReviews() {
        mDatabase.child("usersChallenge").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allReviews.clear();
                long countPending = 0;
                long countCompleted = 0;
                long countRejected = 0;

                // Iterate through all Users
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    // Iterate through all Challenges for that user
                    for (DataSnapshot challengeSnapshot : userSnapshot.getChildren()) {
                        String status = challengeSnapshot.child("status").getValue(String.class);
                        String challengeId = challengeSnapshot.child("challengID").getValue(String.class);
                        String proofUrl = challengeSnapshot.child("proofImage").getValue(String.class);
                        String pushId = challengeSnapshot.getKey();

                        // Add ALL items to master list
                        allReviews.add(new AdminReviewAdapter.ReviewItem(
                                userId, pushId, challengeId, proofUrl, status
                        ));

                        // Count Stats
                        if ("Pending".equalsIgnoreCase(status)) countPending++;
                        else if ("Completed".equalsIgnoreCase(status)) countCompleted++;
                        else if ("Rejected".equalsIgnoreCase(status)) countRejected++;
                    }
                }

                // Update UI Counters
                tvPending.setText(String.valueOf(countPending));
                tvCompleted.setText(String.valueOf(countCompleted));
                tvRejected.setText(String.valueOf(countRejected));

                // Apply current filter (Default: Pending)
                int checkedId = chipGroupFilter.getCheckedChipId();
                String currentStatus = "Pending";
                if (checkedId == R.id.chipCompleted) currentStatus = "Completed";
                else if (checkedId == R.id.chipRejected) currentStatus = "Rejected";

                filterData(currentStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminReviewSubmissionsActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterData(String status) {
        filteredList.clear();
        for (AdminReviewAdapter.ReviewItem item : allReviews) {
            if (item.status != null && item.status.equalsIgnoreCase(status)) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No " + status + " submissions.");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}