package com.example.amadapp.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.EducationContent;
import com.example.amadapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageEducationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private TextView tvContentCount; // New TextView for counter
    private AdminContentAdapter adapter;
    private List<AdminContentAdapter.AdminContentItem> contentList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_education);

        // Init Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Education");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mDatabase = FirebaseDatabase.getInstance().getReference("EducationalContents");

        recyclerView = findViewById(R.id.rvContent);
        fabAdd = findViewById(R.id.fabAddContent);
        tvContentCount = findViewById(R.id.tvContentCount); // Initialize View

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contentList = new ArrayList<>();
        adapter = new AdminContentAdapter(contentList, new AdminContentAdapter.OnContentActionListener() {
            @Override
            public void onEdit(AdminContentAdapter.AdminContentItem item) {
                // Open Activity for Editing
                openAddEditActivity(item);
            }

            @Override
            public void onDelete(AdminContentAdapter.AdminContentItem item) {
                confirmDelete(item);
            }
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> openAddEditActivity(null));

        fetchContent();
    }

    private void fetchContent() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    EducationContent content = data.getValue(EducationContent.class);
                    if (content != null) {
                        contentList.add(new AdminContentAdapter.AdminContentItem(data.getKey(), content));
                    }
                }
                adapter.notifyDataSetChanged();

                // Update Counter Text
                tvContentCount.setText("Total Contents: " + contentList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminManageEducationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddEditActivity(AdminContentAdapter.AdminContentItem item) {
        Intent intent = new Intent(this, AdminAddContentActivity.class);
        if (item != null) {
            intent.putExtra("id", item.id);
            intent.putExtra("title", item.content.getTitle());
            intent.putExtra("content", item.content.getContent());
            intent.putExtra("image", item.content.getImage());
            intent.putExtra("url", item.content.getUrl());
            intent.putExtra("date", item.content.getDate());
        }
        startActivity(intent);
    }

    private void confirmDelete(AdminContentAdapter.AdminContentItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Content")
                .setMessage("Are you sure you want to delete '" + item.content.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDatabase.child(item.id).removeValue();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}