package com.example.amadapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.amadapp.Model.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TwoFactorVerificationActivity extends AppCompatActivity {

    // UI Components
    private EditText codeEditText;
    private Button verifyButton;
    private ProgressBar verificationProgress;
    private LinearLayout resendCodeLayout;
    private TextView timerText;//, helpText, contactSupportText;
    private String full_name, mail, password, address;
    private double lng,lat;
    // Variables
    private FirebaseAuth Fauth;
    private int activity;
    private CountDownTimer resendTimer;
    private boolean canResendCode = false;
    private static final long RESEND_DELAY = 30000; // 30 seconds
    private ExecutorService executorService;
    private EmailOTPService emailOTPService;
    private String OTPCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_two_factor_verification);

        Fauth = FirebaseAuth.getInstance();


        activity = getIntent().getIntExtra("ACTIVITY",-1);

        if (activity == 2) {
            mail = getIntent().getStringExtra("EMAIL");
        } else if (activity == 1) {
            mail = getIntent().getStringExtra("EMAIL");
            full_name = getIntent().getStringExtra("FULLNAME");
            password = getIntent().getStringExtra("PASSWORD");
            address = getIntent().getStringExtra("ADDRESS");
            lng = getIntent().getDoubleExtra("LONGITUDE",0);
            lat = getIntent().getDoubleExtra("LATITUDE",0);
            Log.e("Data606",mail+" "+full_name+" "+password+" "+address+" "+lng+" "+lat);
        }

        executorService = Executors.newFixedThreadPool(2);
        emailOTPService = new EmailOTPService();

        initializeViews();
        setupClickListeners();
        startResendTimer();

    }

    private void initializeViews() {
        codeEditText = findViewById(R.id.codeEditText);
        verifyButton = findViewById(R.id.verifyButton);
        verificationProgress = findViewById(R.id.verificationProgress);
        resendCodeLayout = findViewById(R.id.resendCodeLayout);
        timerText = findViewById(R.id.timerText);

        SendOTPMail(mail);
    }

    private void setupClickListeners() {
        // Verify Button
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyOTPMail(codeEditText.getText().toString().trim());
            }
        });

        // Resend Code (for authenticator apps, this just shows guidance)
        resendCodeLayout.setOnClickListener(v -> {
            if (canResendCode) {
                showResendGuidance();
            } else {
                Toast.makeText(this, "Please wait for the current code to expire", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void SendOTPMail(String email) {

        OTPCode = generateRandomOTP();
        executorService.execute(() -> {
            boolean emailSent = emailOTPService.sendOTPEmail(email, OTPCode);

            runOnUiThread(() -> {
                if (emailSent) {
                    Toast.makeText(getBaseContext(), "OTP sent successfully to " + email, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getBaseContext(), "Failed to send OTP. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    private void showResendGuidance() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("New Code Generated");
        builder.setMessage("Your authenticator app should now show a new 6-digit code. Please enter the latest code displayed in your app.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            codeEditText.setText("");
            codeEditText.requestFocus();

            // Restart timer
            startResendTimer();
        });

        builder.show();
    }

    private void startResendTimer() {
        canResendCode = false;
        resendCodeLayout.setAlpha(0.5f);
        resendCodeLayout.setClickable(false);

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(RESEND_DELAY, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                timerText.setText("(" + seconds + "s)");
            }

            public void onFinish() {
                canResendCode = true;
                resendCodeLayout.setAlpha(1.0f);
                resendCodeLayout.setClickable(true);
                timerText.setText("(Ready)");
            }
        }.start();
    }
    private void verifyOTPMail(String code) {
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (code.equals(OTPCode)) {

            Toast.makeText(this, "Verification successful!", Toast.LENGTH_LONG).show();

            if (activity==2) {

                    Intent i = new Intent(TwoFactorVerificationActivity.this, MenuActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();

            } else {
                Fauth.createUserWithEmailAndPassword(mail, password)
                        .addOnCompleteListener(TwoFactorVerificationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign-in success
                                    // encryptAllData();
                                    UserData usr_data = new UserData(full_name, mail, password,String.valueOf(lng),
                                            String.valueOf(lat),address);
                                    FirebaseDatabase.getInstance().getReference("Users")
                                            .child(Fauth.getUid())
                                            .setValue(usr_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(TwoFactorVerificationActivity.this, "New Account Created Successful",
                                                                Toast.LENGTH_SHORT).show();

                                                        Intent i = new Intent(TwoFactorVerificationActivity.this, MainActivity.class);
                                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(i);
                                                        finish();

                                                    } else {
                                                        Toast.makeText(TwoFactorVerificationActivity.this, "Registeration is failed!",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(TwoFactorVerificationActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } else {
            Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_LONG).show();

            codeEditText.setText("");

        }
    }

    private String generateRandomOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}