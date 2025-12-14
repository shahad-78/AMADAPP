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

import com.example.amadapp.ChallengeDetailActivity;
import com.example.amadapp.Model.Challenge;
import com.example.amadapp.R;
import com.example.amadapp.ViewChallengesAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChallengesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ViewChallengesAdapter adapter;
    private List<Challenge> challengeList;
    private List<String> challengeKeys; // List to store Firebase Keys
    private DatabaseReference databaseReference;

    public ChallengesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_challenges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvChallenges);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        challengeList = new ArrayList<>();
        challengeKeys = new ArrayList<>(); // Init key list

        // Pass both lists to the adapter
        adapter = new ViewChallengesAdapter(getContext(), challengeList, challengeKeys, (challenge, id) -> {
            // On Click, use the passed ID
            Intent intent = new Intent(getContext(), ChallengeDetailActivity.class);
            intent.putExtra("id", id); // Pass the Firebase Key
            intent.putExtra("title", challenge.getTitle());
            intent.putExtra("desc", challenge.getDescription());
            intent.putExtra("start_date", challenge.getStart_date());
            intent.putExtra("end_date", challenge.getEnd_date());
            intent.putExtra("points", challenge.getPoints());
            intent.putExtra("image_url", challenge.getImage_url());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Fetch Data
        databaseReference = FirebaseDatabase.getInstance().getReference("Challenges");
        fetchChallenges();
    }

    private void fetchChallenges() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                challengeList.clear();
                challengeKeys.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Challenge challenge = data.getValue(Challenge.class);
                        if (challenge != null) {
                            challengeList.add(challenge);
                            challengeKeys.add(data.getKey()); // Store the Key separately
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No active challenges found.", Toast.LENGTH_SHORT).show();
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