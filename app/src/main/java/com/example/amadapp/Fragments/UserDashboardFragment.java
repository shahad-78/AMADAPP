package com.example.amadapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amadapp.Model.Challenge;
import com.example.amadapp.Model.RecentActivity;
import com.example.amadapp.Model.UserTree;
import com.example.amadapp.Model.UsersChalleng;
import com.example.amadapp.R;
import com.example.amadapp.UserChallengeAdapter;
import com.example.amadapp.UserProfileActivity;
import com.example.amadapp.UserTreeAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDashboardFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private String user_name = "";

    // UI Components
    private TextView welcome_txt;
    private ImageView profileImage; // NEW: Profile Image View
    private TextView tvTreesCount, tvChallengesCount, tvPointsCount;
    private RecyclerView activeChallengesRecyclerView;
    private RecyclerView myTreesRecyclerView;

    private UserChallengeAdapter challengeAdapter;
    private List<UsersChalleng> challengeList;

    private UserTreeAdapter treeAdapter;
    private List<UserTree> userTreeList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserID;

    public UserDashboardFragment() {
    }

    public static UserDashboardFragment newInstance(String param1) {
        UserDashboardFragment fragment = new UserDashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user_name = getArguments().getString(ARG_NAME);
        }

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserID = mAuth.getCurrentUser().getUid();
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        welcome_txt = view.findViewById(R.id.welcome_txt);
        if (!user_name.isEmpty()) {
            welcome_txt.setText("Welcome, " + user_name);
        }

        // --- NEW: Profile Image Click Listener ---
        profileImage = view.findViewById(R.id.profileImage);
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
            });
        }

        tvTreesCount = view.findViewById(R.id.treesCountText);
        tvChallengesCount = view.findViewById(R.id.challengesCountText);
        tvPointsCount = view.findViewById(R.id.pointsCountText);

        activeChallengesRecyclerView = view.findViewById(R.id.activeChallengesRecyclerView);
        myTreesRecyclerView = view.findViewById(R.id.myTreesRecyclerView);

        challengeList = new ArrayList<>();
        userTreeList = new ArrayList<>();


        setupActiveChallengesList();
        setupMyTreesList();

        if (currentUserID != null) {
            fetchJoinedChallenges();
            fetchMyTrees();
            calculateUserStats();
        }
    }

    private void setupActiveChallengesList() {
        challengeAdapter = new UserChallengeAdapter(challengeList);
        activeChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activeChallengesRecyclerView.setAdapter(challengeAdapter);
        activeChallengesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupMyTreesList() {
        treeAdapter = new UserTreeAdapter(userTreeList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        myTreesRecyclerView.setLayoutManager(layoutManager);
        myTreesRecyclerView.setAdapter(treeAdapter);
    }

    // --- Data Fetching Logic ---

    private void calculateUserStats() {
        mDatabase.child("userTrees").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long treeCount = snapshot.getChildrenCount();
                tvTreesCount.setText(String.valueOf(treeCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mDatabase.child("usersChallenge").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long completedCount = 0;
                List<String> completedChallengeIds = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    UsersChalleng uc = data.getValue(UsersChalleng.class);
                    if (uc != null && "Completed".equalsIgnoreCase(uc.getStatus())) {
                        completedCount++;
                        completedChallengeIds.add(uc.getChallengID());
                    }
                }

                tvChallengesCount.setText(String.valueOf(completedCount));

                if (!completedChallengeIds.isEmpty()) {
                    calculateTotalPoints(completedChallengeIds);
                } else {
                    tvPointsCount.setText("0");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void calculateTotalPoints(List<String> challengeIds) {
        mDatabase.child("Challenges").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalPoints = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (challengeIds.contains(data.getKey())) {
                        Challenge c = data.getValue(Challenge.class);
                        if (c != null) {
                            totalPoints += c.getPoints();
                        }
                    }
                }
                tvPointsCount.setText(String.valueOf(totalPoints));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchJoinedChallenges() {
        mDatabase.child("usersChallenge").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        challengeList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            UsersChalleng userChallenge = data.getValue(UsersChalleng.class);
                            if (userChallenge != null && ("Joined".equals(userChallenge.getStatus()) || "Pending".equals(userChallenge.getStatus()))) {
                                challengeList.add(userChallenge);
                            }
                        }
                        challengeAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchMyTrees() {
        mDatabase.child("userTrees").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userTreeList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            UserTree userTree = data.getValue(UserTree.class);
                            if (userTree != null) {
                                userTreeList.add(userTree);
                            }
                        }
                        treeAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


}