package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TodoActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageButton addtaskBtn;
    private TextView taskGroupNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        // Initialize views
        backBtn = findViewById(R.id.backbtn);
        addtaskBtn = findViewById(R.id.addtaskbtn);
        taskGroupNameTextView = findViewById(R.id.taskGroupNameTextView);

        // Get data from intent
        Intent intent = getIntent();
        String taskGroupId = intent.getStringExtra("TASK_GROUP_ID");
        String taskGroupName = intent.getStringExtra("TASK_GROUP_NAME");

        // Set task group name in TextView (optional)
        if (taskGroupName != null) {
            taskGroupNameTextView.setText(taskGroupName);
        }

        // Handle back button click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(TodoActivity.this, ProjectsActivity.class);
                startActivity(backIntent);
            }
        });

        // Handle add task button click
        addtaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addtaskIntent = new Intent(TodoActivity.this, AddTaskActivity.class);
                // Optionally pass task group ID to AddTaskActivity
                addtaskIntent.putExtra("TASK_GROUP_ID", taskGroupId);

                // passed TaskGroupName to re-render ToDoActivity after creating task.
                addtaskIntent.putExtra("TASK_GROUP_NAME", taskGroupName);
                startActivity(addtaskIntent);
            }
        });
    }
}
