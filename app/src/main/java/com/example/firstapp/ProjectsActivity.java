package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ProjectsActivity extends AppCompatActivity {
    private ImageButton userBtn;
    private ImageButton newprojectBtn;
    private ImageButton sampleproject1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        userBtn = findViewById(R.id.user);
        newprojectBtn = findViewById(R.id.newprojectbtn);
        sampleproject1 = findViewById(R.id.SampleProject1);

        // OnClickListener for userBtn
        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to UserPageActivity
                Intent userIntent = new Intent(ProjectsActivity.this, UserPageActivity.class);
                startActivity(userIntent);
            }
        });

        // OnClickListener for newprojectBtn
        newprojectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to AddNewGroupActivity
                Intent addNewGroupIntent = new Intent(ProjectsActivity.this, AddNewGroupActivity.class);
                startActivity(addNewGroupIntent);
            }
        });

        sampleproject1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to AddNewGroupActivity
                Intent sampleprojectIntent = new Intent(ProjectsActivity.this, TodoActivity.class);
                startActivity(sampleprojectIntent);
            }
        });
    }
}
