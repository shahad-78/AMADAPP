package com.example.amadapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.amadapp.Admin.AdminDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView registerLink;
    private Button loginButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth Fauth;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Fauth = FirebaseAuth.getInstance();
        registerLink = findViewById(R.id.registerLink);
        loginButton = findViewById(R.id.loginButton);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }


        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();

                if(email.equals("admin@admin.com") && password.equals("admin")){
                    Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

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
                Fauth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.e("WAWAW",email + password);
                                //FirebaseUser user = auth.getCurrentUser();
                                Toast.makeText(getBaseContext(),"Login Success", Toast.LENGTH_SHORT).show();
                                      redirectTo2FAVerification();
                            } else {
                                Toast.makeText(getBaseContext(),"Login Failed, Try again",Toast.LENGTH_SHORT).show();
                                Log.e("Login", "Error: " + task.getException().getMessage());
                            }
                        });

                /*Intent intent = new Intent(MainActivity.this, TwoFactorVerificationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();*/
            }

        });
}

    private void redirectTo2FAVerification() {
        // Get the secret key from storage

        Intent intent = new Intent(MainActivity.this, TwoFactorVerificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("EMAIL", email);
        intent.putExtra("ACTIVITY", 2);
        startActivity(intent);

        // Don't finish() here - user might press back to re-enter credentials
    }

    private void redirectToMainApp() {
        //Log.d("LOGIN_2FA", "Redirecting to main app for user: " + userId);

        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        //intent.putExtra("USER_ID", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}