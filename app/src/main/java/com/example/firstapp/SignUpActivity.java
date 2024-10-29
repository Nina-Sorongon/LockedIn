package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private TextView loginTextView;
    private Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Buttons
        loginTextView = findViewById(R.id.loginbtn);
        signupBtn = findViewById(R.id.signupbtn);

        // Set an OnClickListener for the TextView
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to the LoginActivity when clicked
                Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect view to ProjectsActivity when clicked
                Intent projectsIntent = new Intent(SignUpActivity.this, ProjectsActivity.class);
                startActivity(projectsIntent);
            }
        });
    }
}
