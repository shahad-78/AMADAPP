package com.example.amadapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.amadapp.Fragments.ChallengesFragment;
import com.example.amadapp.Fragments.ResourcesFragment;
import com.example.amadapp.Fragments.TreeFragment;
import com.example.amadapp.Fragments.UserDashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {

    private FirebaseAuth Fauth;
    private String UID;
    private TextView welcome_txt;
    private BottomNavigationView bottomNavigationView;
    String name = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

       Fauth = FirebaseAuth.getInstance();
        //welcome_txt = findViewById(R.id.welcome_txt);

        readUserData();

        bottomNavigationView = findViewById(R.id.bottom_navigation);


        // Set the listener for the bottom navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();

                // Check which icon was clicked and select the correct Fragment
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new UserDashboardFragment().newInstance(name);
                } else if (itemId == R.id.nav_plant_trees) {
                    selectedFragment = new TreeFragment();
                    //showToast("Plant Trees clicked (Not implemented)");
                } else if (itemId ==R.id.nav_challenges) {
                    selectedFragment = new ChallengesFragment();
                    //showToast("Challenges clicked (Not implemented)");
                } else if (itemId == R.id.nav_resources) {
                    selectedFragment = new ResourcesFragment();
                    //showToast("Resources clicked (Not implemented)");
                }

                // If a fragment was selected, load it
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }

                return false; // Item not handled
            }
        });


    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // This line replaces the content of R.id.fragment_container with the new fragment
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void readUserData() {
        UID = Fauth.getCurrentUser().getUid();
        Log.e("UID11",UID);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // 2. Query the specific child (the UID)
        // We use addListenerForSingleValueEvent to read the data only once.
        usersRef.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 3. Check if the user data exists
                if (dataSnapshot.exists()) {

                    // 4. Get the value of the "username" field.
                    // IMPORTANT: Change "username" if your field has a different name.
                    String username = dataSnapshot.child("encryptedFullName").getValue(String.class);
                    Log.e("NAME101", username);
                    if (username != null) {
                        // Success! Pass the username back via the callback.
                        try {
                            name = EncryptionHelper.decrypt(username);
                            //welcome_txt.setText("Welcome, " + EncryptionHelper.decrypt(username));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    } else {
                        // The user exists, but the "username" field is missing or null.
                        //welcome_txt.setText("Welcome, ");
                        name = "";

                    }
                    loadFragment(new UserDashboardFragment().newInstance(name));
                } else {
                    // The user with this UID was not found in the database.
                    Toast.makeText(getBaseContext(), "User Not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 5. Handle any errors that occurred during the read
                //callback.onError(databaseError);
                Log.e("ERROR", databaseError.getMessage());
            }
        });

    }
}