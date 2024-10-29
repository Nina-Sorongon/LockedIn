package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class UserPageActivity extends AppCompatActivity {
    private Button logoutBtn;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        logoutBtn = findViewById(R.id.logout);
        backBtn = findViewById(R.id.backbtn);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(UserPageActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(UserPageActivity.this, ProjectsActivity.class);
                startActivity(backIntent);
            }
        });


    }
}
