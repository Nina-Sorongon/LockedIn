package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TodoActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageButton addtaskBtn;
    private TextView taskGroupNameTextView;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Map<String, Object>> tasks;
    private List<Map<String, Object>> filteredTasks = new ArrayList<>();
    private boolean isFilterSpinnerInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        // Initialize views
        backBtn = findViewById(R.id.backbtn);
        addtaskBtn = findViewById(R.id.addtaskbtn);
        taskGroupNameTextView = findViewById(R.id.taskGroupNameTextView);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        Spinner filterDropdown = findViewById(R.id.filterDropdown);

        // Get data from Intent
        Intent intent = getIntent();
        String taskGroupId = intent.getStringExtra("TASK_GROUP_ID");
        String taskGroupName = intent.getStringExtra("TASK_GROUP_NAME");

        // Set up RecyclerView
        tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(tasks, taskGroupId, this);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(taskAdapter);

        // Set task group name in TextView
        if (taskGroupName != null) {
            taskGroupNameTextView.setText(taskGroupName);
        }

        // Load tasks from Firestore with default "All" filter
        if (taskGroupId != null) {
            loadTasksFromFirestore(taskGroupId, "All");
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

        // Handle dropdown selection for filtering
        filterDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFilterSpinnerInitialized) {
                    String selectedFilter = parent.getItemAtPosition(position).toString();
                    // Reload tasks with the selected filter
                    loadTasksFromFirestore(taskGroupId, selectedFilter);
                } else {
                    isFilterSpinnerInitialized = true; // Ignore the initial selection
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default to "All" filter
                loadTasksFromFirestore(taskGroupId, "All");
            }
        });
    }

    private void loadTasksFromFirestore(String taskGroupId, String filter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("taskGroups")
                .document(taskGroupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tasks.clear(); // Clear existing tasks
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null) {
                            tasks.addAll(taskList); // Populate tasks
                        }

                        sortTasksByDeadline(tasks); // Sort tasks by deadline

                        // Apply the selected filter
                        applyFilter(filter);
                    } else {
                        Toast.makeText(this, "Task group document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void applyFilter(String filter) {
        filteredTasks.clear();

        if ("All".equals(filter)) {
            filteredTasks.addAll(tasks);
        } else {
            for (Map<String, Object> task : tasks) {
                boolean status = task.get("status") != null && (boolean) task.get("status");
                if ("Ongoing".equals(filter) && !status) {
                    filteredTasks.add(task);
                } else if ("Completed".equals(filter) && status) {
                    filteredTasks.add(task);
                }
            }
        }

        sortTasksByDeadline(filteredTasks);
        taskAdapter.updateTaskList(new ArrayList<>(filteredTasks)); // Pass a new instance
    }


    private void sortTasksByDeadline(List<Map<String, Object>> taskList) {
        Collections.sort(taskList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> task1, Map<String, Object> task2) {
                long deadline1 = (long) task1.get("deadline");
                long deadline2 = (long) task2.get("deadline");
                return Long.compare(deadline1, deadline2);
            }
        });
    }
}
