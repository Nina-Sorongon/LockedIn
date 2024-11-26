package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for user login.
 * This activity provides authentication functionality and navigation to sign-up and dashboard activities.
 */
public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginBtn;

    /**
     * Initializes the activity and sets up UI elements for login and navigation.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginbtn);
        TextView signupBtn = findViewById(R.id.signupbtn);

        // If redirected from sign-up, pre-fill the email field
        String signedUpUserEmail = getIntent().getStringExtra("email");
        if (signedUpUserEmail != null) {
            emailField.setText(signedUpUserEmail);
        }

        // Set up login button
        loginBtn.setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            try {
                // Validate input
                if (email.isEmpty()) {
                    throw new IllegalArgumentException("Email field cannot be empty.");
                }
                if (password.isEmpty()) {
                    throw new IllegalArgumentException("Password field cannot be empty.");
                }

                // Authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, ProjectsActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IllegalArgumentException e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("LoginActivity", "Unexpected error during login", e);
                Toast.makeText(LoginActivity.this, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up sign-up navigation button
        signupBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
