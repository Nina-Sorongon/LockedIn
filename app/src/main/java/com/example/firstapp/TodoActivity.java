package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class TodoActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageButton addtaskBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        backBtn = findViewById(R.id.backbtn);
        addtaskBtn = findViewById(R.id.addtaskbtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(TodoActivity.this, ProjectsActivity.class);
                startActivity(backIntent);
            }
        });

        addtaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addtaskIntent = new Intent(TodoActivity.this, AddTaskActivity.class);
                startActivity(addtaskIntent);
            }
        });
    }
}