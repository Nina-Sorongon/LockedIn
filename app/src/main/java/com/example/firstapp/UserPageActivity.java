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

public class UserPageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button logoutBtn;
    private ImageButton backBtn;
    private TextView emailText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        logoutBtn = findViewById(R.id.logout);
        backBtn = findViewById(R.id.backbtn);
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);

        // Fetch and display user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            emailText.setText(email != null ? email : "No email available");
            passwordText.setText("************"); // Placeholder for the password
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
        }

        // Logout button functionality
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent mainIntent = new Intent(UserPageActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

        // Back button functionality
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(UserPageActivity.this, ProjectsActivity.class);
                startActivity(backIntent);
            }
        });
    }
}
