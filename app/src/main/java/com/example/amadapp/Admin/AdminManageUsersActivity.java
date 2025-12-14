package com.example.amadapp.Admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.EncryptionHelper;
import com.example.amadapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvUserCount; // New Counter View

    private AdminUsersAdapter adapter;
    private List<AdminUser> userList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        // Init Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Init Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Init Views
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvUserCount = findViewById(R.id.tvUserCount); // Initialize
        recyclerView = findViewById(R.id.rvUsers);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        // Setup Adapter with Delete Listener
        adapter = new AdminUsersAdapter(userList, this::confirmDeleteUser);
        recyclerView.setAdapter(adapter);

        fetchUsers();
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String userId = data.getKey();
                        String encName = data.child("encryptedFullName").getValue(String.class);
                        String encEmail = data.child("encryptedEmail").getValue(String.class);

                        try {
                            String name = (encName != null) ? EncryptionHelper.decrypt(encName) : "Unknown";
                            String email = (encEmail != null) ? EncryptionHelper.decrypt(encEmail) : "Unknown";

                            userList.add(new AdminUser(userId, name, email));
                        } catch (Exception e) {
                            e.printStackTrace();
                            userList.add(new AdminUser(userId, "Error Decrypting", "Error"));
                        }
                    }
                }

                if (userList.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();

                // Update Counter Text
                tvUserCount.setText("Total Users: " + userList.size());

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminManageUsersActivity.this, "Error fetching users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteUser(AdminUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(String userId) {
        // Prepare atomic updates to delete User info, User Trees, and User Challenges
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> updates = new HashMap<>();
        updates.put("Users/" + userId, null);
        updates.put("userTrees/" + userId, null);
        updates.put("usersChallenge/" + userId, null);

        // Perform multi-path update
        rootRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            // Success: Now handle DegradedAreas (requires Query)
            cleanupUserReports(userId);
            Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Helper to delete reports where the userID matches
    private void cleanupUserReports(String userId) {
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("DegradedAreas");
        reportsRef.orderByChild("userID").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    data.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error silently
            }
        });
    }
}