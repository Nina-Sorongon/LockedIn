package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The main activity of the application, serving as the entry point.
 * This activity provides buttons for navigation to the login and sign-up activities.
 */
public class MainActivity extends AppCompatActivity {

    private Button loginBtn;
    private Button signupBtn;

    /**
     * Initializes the main activity and sets up click listeners for navigation buttons.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        loginBtn = findViewById(R.id.loginbtn);
        signupBtn = findViewById(R.id.signupbtn);

        // Set click listener for the Log In button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to LoginActivity
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        // Set click listener for the Sign Up button
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to SignUpActivity
                Intent signupIntent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(signupIntent);
            }
        });
    }
}
