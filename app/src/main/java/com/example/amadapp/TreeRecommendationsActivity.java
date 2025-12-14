package com.example.amadapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.Tree;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TreeRecommendationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvLocationInfo, tvEmptyState;
    private TreeRecommendationsAdapter adapter;
    private List<Tree> treeList;
    private List<String> treeKeys; // To store Firebase IDs
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tree_recommendations);

        // Init Views
        recyclerView = findViewById(R.id.rvTrees);
        progressBar = findViewById(R.id.progressBar);
        tvLocationInfo = findViewById(R.id.tvLocationInfo);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Init Lists
        treeList = new ArrayList<>();
        treeKeys = new ArrayList<>();

        // Init Adapter with OnItemClickListener
        adapter = new TreeRecommendationsAdapter(this, treeList, treeKeys, (tree, treeID) -> {
            // Open Upload Activity
            Intent intent = new Intent(TreeRecommendationsActivity.this, UploadPlantedTreeActivity.class);
            intent.putExtra("treeID", treeID); // Pass the Firebase Key
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Get Data from Intent
        String latStr = getIntent().getStringExtra("lat");
        String lngStr = getIntent().getStringExtra("lng");

        if (latStr != null && lngStr != null) {
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);

            String region = determineRegion(lat, lng);
            tvLocationInfo.setText("Based on location "+"\nDetected Region: " + region);

            fetchTrees(region);
        } else {
            Toast.makeText(this, "Location data missing", Toast.LENGTH_SHORT).show();
        }
    }

    private String determineRegion(double lat, double lng) {
        // Logic for Saudi Arabia regions
        if (lat > 28.0) {
            return "Northern";
        } else if (lat < 21.0) {
            return "Southern";
        } else if (lng > 48.0) {
            return "Eastern";
        } else if (lng < 42.0) {
            return "Western";
        }
        // Fallback for the central area (Riyadh, Qassim, etc.)
        return "Central";
    }

    private void fetchTrees(String userRegion) {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference = FirebaseDatabase.getInstance().getReference("Trees");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                treeList.clear();
                treeKeys.clear(); // Clear keys too

                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Tree tree = data.getValue(Tree.class);
                        if (tree != null) {
                            if (tree.getRegion() == null ||
                                    tree.getRegion().equalsIgnoreCase("All") ||
                                    tree.getRegion().equalsIgnoreCase(userRegion)) {

                                treeList.add(tree);
                                treeKeys.add(data.getKey()); // Store the key
                            }
                        }
                    }
                }

                if (treeList.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TreeRecommendationsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}