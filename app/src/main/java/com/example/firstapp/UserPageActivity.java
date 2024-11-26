package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity that displays the user's profile page with email and password details.
 * Provides functionality for logging out and navigating back to previous screens.
 */
public class UserPageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Firebase authentication instance
    private Button logoutBtn; // Button to log out the user
    private ImageButton backBtn; // Button to navigate back to the previous screen
    private TextView emailText, passwordText; // TextViews for displaying user email and password

    /**
     * Called when the activity is first created.
     * Initializes the UI elements, Firebase authentication, and sets up button listeners.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        // Initialize Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Find views by their IDs
        logoutBtn = findViewById(R.id.logout);
        backBtn = findViewById(R.id.backbtn);
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);

        // Fetch and display user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Display email and password (password is a placeholder here for security reasons)
            String email = currentUser.getEmail();
            emailText.setText(email != null ? email : "No email available");
            passwordText.setText("************"); // Placeholder for the password
        } else {
            // Notify the user if no user is logged in
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
        }

        // Set up logout button to sign out and navigate to the MainActivity
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut(); // Sign out the current user
                Intent mainIntent = new Intent(UserPageActivity.this, MainActivity.class);
                startActivity(mainIntent); // Navigate to the MainActivity
                finish(); // Finish the current activity
            }
        });

        // Set up back button to navigate to the ProjectsActivity
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(UserPageActivity.this, ProjectsActivity.class);
                startActivity(backIntent); // Navigate back to the ProjectsActivity
            }
        });
    }
}
