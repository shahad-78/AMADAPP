package com.example.amadapp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, passwordEditText, locationEditText;
    private View strengthIndicator1, strengthIndicator2, strengthIndicator3, strengthIndicator4;
    private TextView passwordStrengthText;
    private Button registerButton;
    private ImageView locationButton;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLatitude;
    private double currentLongitude;
    private String currentAddress;


    private FirebaseAuth Fauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_register);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Fauth = FirebaseAuth.getInstance();
        initializeViews();

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });
        setupPasswordStrengthChecker();
        setupRegistration();
    }

    private void initializeViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        locationEditText = findViewById(R.id.locationEditText);
        strengthIndicator1 = findViewById(R.id.strengthIndicator1);
        strengthIndicator2 = findViewById(R.id.strengthIndicator2);
        strengthIndicator3 = findViewById(R.id.strengthIndicator3);
        strengthIndicator4 = findViewById(R.id.strengthIndicator4);
        passwordStrengthText = findViewById(R.id.passwordStrengthText);
        registerButton = findViewById(R.id.registerButton);
        locationButton = findViewById(R.id.locationButton);

        // Setup login link
        TextView loginLink = findViewById(R.id.loginLink);
        loginLink.setOnClickListener(v -> {
            // Navigate to login activity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void setupPasswordStrengthChecker() {
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkPasswordStrength(s.toString());
                //validateForm();
            }
        });

        // Also validate form when other fields change
        TextWatcher formValidator = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //validateForm();
            }
        };

        fullNameEditText.addTextChangedListener(formValidator);
        emailEditText.addTextChangedListener(formValidator);
        locationEditText.addTextChangedListener(formValidator);
    }

    private void checkPasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);

        // Reset all indicators
        resetIndicators();

        switch (strength) {
            case 0: // Very Weak
                setIndicatorStrength(1, "Very Weak - Minimum 8 characters", R.color.weak_password);
                break;
            case 1: // Weak
                setIndicatorStrength(1, "Weak - Add uppercase letters", R.color.weak_password);
                break;
            case 2: // Medium
                setIndicatorStrength(2, "Medium - Add numbers", R.color.medium_password);
                break;
            case 3: // Strong
                setIndicatorStrength(3, "Strong - Good password", R.color.strong_password);
                break;
            case 4: // Very Strong
                setIndicatorStrength(4, "Very Strong - Excellent!", R.color.very_strong_password);
                break;
        }
    }

    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        int strength = 0;
        // Ceck length (base requirement)
        if (password.length() >= 8) {
            strength = 1; // Basic strength
            // Additional criteria
            boolean hasUpper = !password.equals(password.toLowerCase());
            boolean hasLower = !password.equals(password.toUpperCase());
            boolean hasDigit = password.matches(".*\\d.*");
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

            if (hasUpper && hasLower) strength++;
            if (hasDigit) strength++;
            if (hasSpecial) strength++;

            // Bonus for longer passwords
            if (password.length() >= 12) strength = Math.min(strength + 1, 4);
        }

        return Math.min(strength, 4);
    }

    private void resetIndicators() {
        int grayColor = ContextCompat.getColor(this, R.color.weak_password);
        strengthIndicator1.setBackgroundColor(grayColor);
        strengthIndicator2.setBackgroundColor(grayColor);
        strengthIndicator3.setBackgroundColor(grayColor);
        strengthIndicator4.setBackgroundColor(grayColor);
    }

    private void setIndicatorStrength(int level, String text, int colorRes) {
        int color = ContextCompat.getColor(this, colorRes);

        strengthIndicator1.setBackgroundColor(level >= 1 ? color : ContextCompat.getColor(this, R.color.weak_password));
        strengthIndicator2.setBackgroundColor(level >= 2 ? color : ContextCompat.getColor(this, R.color.weak_password));
        strengthIndicator3.setBackgroundColor(level >= 3 ? color : ContextCompat.getColor(this, R.color.weak_password));
        strengthIndicator4.setBackgroundColor(level >= 4 ? color : ContextCompat.getColor(this, R.color.weak_password));

        passwordStrengthText.setText(text);
        passwordStrengthText.setTextColor(color);
    }

    private void validateForm() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String location = locationEditText.getText().toString().trim();

        boolean isNameValid = !fullName.isEmpty();
        boolean isEmailValid = isValidEmail(email);
        boolean isPasswordValid = calculatePasswordStrength(password) >= 2; // At least medium strength
        boolean isLocationValid = !location.isEmpty();

        boolean isFormValid = isNameValid && isEmailValid && isPasswordValid && isLocationValid;

        registerButton.setEnabled(isFormValid);
        registerButton.setAlpha(isFormValid ? 1.0f : 0.5f);
    }

    private boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            return false;
        }
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void setupRegistration() {
        registerButton.setOnClickListener(v -> {
            //if (validateFormFinal()) {
                performRegistration();
            //}
        });

        // Setup location button
        locationButton.setOnClickListener(v -> {
            getCurrentLocation();
        });
    }

    private boolean validateFormFinal() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String location = locationEditText.getText().toString().trim();

        // Validate full name
        if (fullName.isEmpty()) {
            showError(fullNameEditText, "Please enter your full name");
            return false;
        }

        // Validate email
        if (email.isEmpty()) {
            showError(emailEditText, "Please enter your email address");
            return false;
        }

        if (!isValidEmail(email)) {
            showError(emailEditText, "Please enter a valid email address");
            return false;
        }

        // Validate password strength
        int passwordStrength = calculatePasswordStrength(password);
        if (passwordStrength < 2) {
            Toast.makeText(this, "Please use a stronger password (at least 8 characters with mixed case)", Toast.LENGTH_LONG).show();
            passwordEditText.requestFocus();
            return false;
        }

        // Validate location
        if (location.isEmpty()) {
            showError(locationEditText, "Please enter your location");
            return false;
        }

        return true;
    }

    private void showError(EditText editText, String message) {
        editText.setError(message);
        editText.requestFocus();
    }

    private void performRegistration()  {
        // Get form data
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String location = locationEditText.getText().toString().trim();


        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("Enter your name");
            return;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter valid email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Enter your password");
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            locationEditText.setError("Click on location button");
            return;
        }

        Intent intent = new Intent(RegisterActivity.this, TwoFactorVerificationActivity.class);
        intent.putExtra("FULLNAME", fullName);
        intent.putExtra("EMAIL", email);
        intent.putExtra("PASSWORD", password);
        intent.putExtra("LATITUDE", currentLatitude);
        intent.putExtra("LONGITUDE", currentLongitude);
        intent.putExtra("ADDRESS", currentAddress);
        intent.putExtra("ACTIVITY", 1);
        startActivity(intent);
        /*auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in success
                            Toast.makeText(RegisterActivity.this, "Reg Successful", Toast.LENGTH_SHORT).show();
                           // encryptAllData();
                            UserData usr_data = new UserData(fullName, email, password,String.valueOf(currentLongitude),String.valueOf(currentLatitude),currentAddress);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(auth.getUid())
                                    .setValue(usr_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                registerButton.setEnabled(false);
                                                registerButton.setText("Creating Account...");

                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Registeration is failed!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/

    }

    /*private void encryptAllData(String fullName, String email, String password, String s, String s1, String currentAddress) {

        try {
            encryptedFullName = EncryptionHelper.encrypt(fullName);
            encryptedEmail = EncryptionHelper.encrypt(email);
            encryptedPass = EncryptionHelper.encrypt(password);
            encryptedAddress = EncryptionHelper.encrypt(currentAddress);
            encryptedLng = EncryptionHelper.encrypt(s);
            encryptedLat = EncryptionHelper.encrypt(s1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }

    private void getCurrentLocation() {
        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            fetchLocation();
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            // Get address from coordinates
                            getAddressFromLocation(currentLatitude, currentLongitude);

                        } else {
                            locationEditText.setText("Unable to get location");
                            Toast.makeText(RegisterActivity.this,
                                    "Location not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Build address string
                StringBuilder addressBuilder = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressBuilder.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressBuilder.append(", ");
                    }
                }

                currentAddress = addressBuilder.toString();

                // Display location information
                displayLocationInfo(latitude, longitude, currentAddress);

            } else {
                locationEditText.setText("Address not found");
            }

        } catch (IOException e) {
            e.printStackTrace();
            locationEditText.setText("Error getting address");
            Toast.makeText(this, "Geocoder service not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayLocationInfo(double lat, double lng, String address) {
        String locationText = "Latitude: " + lat +
                "\nLongitude: " + lng +
                "\nAddress: " + address;

        locationEditText.setText(address);

        // You can also use these values as needed
        // currentLatitude, currentLongitude, currentAddress are now available
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                fetchLocation();
            } else {
                // Permission denied
                Toast.makeText(this,
                        "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Getters to access location data from other parts of your app
    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }
}