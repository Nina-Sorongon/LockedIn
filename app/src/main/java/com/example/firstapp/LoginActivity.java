package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private TextView signupTextView;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Buttons
        signupTextView = findViewById(R.id.signupbtn);
        loginBtn = findViewById(R.id.loginbtn);

        // OnClickListener for the "if u alr have an acc signup here" thing
        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to the SignUpActivity when clicked
                Intent signupIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signupIntent);
            }
        });

        // OnClickListener for the login Button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect view to ProjectsActivity when clicked
                Intent projectsIntent = new Intent(LoginActivity.this, ProjectsActivity.class);
                startActivity(projectsIntent);
            }
        });
    }
}
