package com.example.amadapp.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.Tree;
import com.example.amadapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageTreesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private TextView tvTreeCount; // New Counter View
    private AdminTreesAdapter adapter;
    private List<AdminTreesAdapter.AdminTreeItem> treeList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_trees);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Trees");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mDatabase = FirebaseDatabase.getInstance().getReference("Trees");

        recyclerView = findViewById(R.id.rvTrees);
        fabAdd = findViewById(R.id.fabAddTree);
        tvTreeCount = findViewById(R.id.tvTreeCount); // Initialize

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        treeList = new ArrayList<>();

        adapter = new AdminTreesAdapter(treeList, new AdminTreesAdapter.OnTreeActionListener() {
            @Override
            public void onEdit(AdminTreesAdapter.AdminTreeItem item) {
                openAddEditActivity(item);
            }

            @Override
            public void onDelete(AdminTreesAdapter.AdminTreeItem item) {
                confirmDelete(item);
            }
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> openAddEditActivity(null));

        fetchTrees();
    }

    private void fetchTrees() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                treeList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Tree tree = data.getValue(Tree.class);
                    if (tree != null) {
                        treeList.add(new AdminTreesAdapter.AdminTreeItem(data.getKey(), tree));
                    }
                }
                adapter.notifyDataSetChanged();

                // Update Counter Text
                tvTreeCount.setText("Total Trees: " + treeList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminManageTreesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddEditActivity(AdminTreesAdapter.AdminTreeItem item) {
        Intent intent = new Intent(this, AdminAddTreeActivity.class);
        if (item != null) {
            intent.putExtra("id", item.id);
            intent.putExtra("name", item.tree.getName());
            intent.putExtra("scientificName", item.tree.getScientificName());
            intent.putExtra("region", item.tree.getRegion());
            intent.putExtra("imageUrl", item.tree.getImageUrl());
            intent.putExtra("description", item.tree.getDescription());
        }
        startActivity(intent);
    }

    private void confirmDelete(AdminTreesAdapter.AdminTreeItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tree")
                .setMessage("Delete " + item.tree.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDatabase.child(item.id).removeValue();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}