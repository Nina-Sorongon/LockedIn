package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TodoActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageButton addtaskBtn;
    private TextView taskGroupNameTextView;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Map<String, Object>> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        // Initialize views
        backBtn = findViewById(R.id.backbtn);
        addtaskBtn = findViewById(R.id.addtaskbtn);
        taskGroupNameTextView = findViewById(R.id.taskGroupNameTextView);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);

        // Get data from Intent
        Intent intent = getIntent();
        String taskGroupId = intent.getStringExtra("TASK_GROUP_ID");
        String taskGroupName = intent.getStringExtra("TASK_GROUP_NAME");

        // Set up RecyclerView
        tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(tasks, taskGroupId);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(taskAdapter);



        // Set task group name in TextView
        if (taskGroupName != null) {
            taskGroupNameTextView.setText(taskGroupName);
        }

        // Load tasks from Firestore
        if (taskGroupId != null) {
            loadTasksFromFirestore(taskGroupId);
        } else {
            Toast.makeText(this, "Task group not found", Toast.LENGTH_SHORT).show();
        }

        // Handle back button click
        backBtn.setOnClickListener(view -> {
            Intent backIntent = new Intent(TodoActivity.this, ProjectsActivity.class);
            startActivity(backIntent);
        });

        // Handle add task button click
        addtaskBtn.setOnClickListener(view -> {
            Intent addtaskIntent = new Intent(TodoActivity.this, AddTaskActivity.class);
            addtaskIntent.putExtra("TASK_GROUP_ID", taskGroupId);
            addtaskIntent.putExtra("TASK_GROUP_NAME", taskGroupName);
            startActivity(addtaskIntent);
        });
    }

    private void loadTasksFromFirestore(String taskGroupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("taskGroups")
                .document(taskGroupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the "tasks" field as a list of maps
                        tasks.clear();
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null) {
                            tasks.addAll(taskList); // Add all tasks to the local list
                            Toast.makeText(this, tasks.size() + " Tasks Loaded Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
                        }
                        taskAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    } else {
                        Toast.makeText(this, "Task group document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
