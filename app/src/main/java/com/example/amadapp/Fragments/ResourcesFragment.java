package com.example.amadapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.ResourcesAdapter;
import com.example.amadapp.Model.EducationContent;
import com.example.amadapp.R;
import com.example.amadapp.ResourceDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ResourcesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ResourcesAdapter adapter;
    private List<EducationContent> resourceList;
    private DatabaseReference databaseReference;

    public ResourcesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resources, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        recyclerView = view.findViewById(R.id.rvResources);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resourceList = new ArrayList<>();

        // Setup Adapter
        adapter = new ResourcesAdapter(getContext(), resourceList, content -> {
            // Handle Item Click: Open Detail Activity
            Intent intent = new Intent(getContext(), ResourceDetailActivity.class);
            intent.putExtra("title", content.getTitle());
            intent.putExtra("content", content.getContent());
            intent.putExtra("date", content.getDate());
            intent.putExtra("image", content.getImage());
            intent.putExtra("url", content.getUrl());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("EducationalContents");

        // Fetch Data
        fetchResources();
    }

    private void fetchResources() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resourceList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        EducationContent content = data.getValue(EducationContent.class);
                        if (content != null) {
                            resourceList.add(content);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No resources found", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}