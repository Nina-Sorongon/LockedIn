package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class ProjectsActivity extends AppCompatActivity {
    private RecyclerView taskGroupsRecyclerView;
    private List<Map<String, Object>> taskGroups;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ActivityResultLauncher<Intent> addGroupLauncher;
    private TextView noTaskGroupText;
    private ImageView noTaskGroupIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and TextView
        taskGroupsRecyclerView = findViewById(R.id.taskGroupsRecyclerView);
        taskGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        noTaskGroupText = findViewById(R.id.noTaskGroupMessage); // Initialize the TextView
        noTaskGroupIcon = findViewById(R.id.noTaskGroupIcon);

        taskGroups = new ArrayList<>();
        taskGroupsRecyclerView.setAdapter(new TaskGroupAdapter());

        // Fetch Task Groups
        fetchTaskGroups();

        // Initialize "Add New Group" button with ActivityResultLauncher
        ImageButton newProjectBtn = findViewById(R.id.newprojectbtn);
        addGroupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshTaskGroups();
                    }
                }
        );

        newProjectBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddNewGroupActivity.class);
            addGroupLauncher.launch(intent);
        });

        ImageButton userButton = findViewById(R.id.user);
        userButton.setOnClickListener(view -> {
            // Navigate to UserPageActivity
            Intent intent = new Intent(ProjectsActivity.this, UserPageActivity.class);
            startActivity(intent);
        });
    }

    private void fetchTaskGroups() {
        taskGroups.clear();

        // Get the currently authenticated user's ID
        String userId = auth.getCurrentUser().getUid();

        // Query Firestore to fetch task groups
        db.collection("taskGroups")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> taskGroup = document.getData();
                        taskGroup.put("id", document.getId()); // Ensure each task group has a unique ID
                        taskGroups.add(taskGroup);
                    }

                    // Notify the RecyclerView adapter of data changes
                    taskGroupsRecyclerView.getAdapter().notifyDataSetChanged();

                    // Update visibility of the no task group message
                    if (taskGroups.isEmpty()) {
                        noTaskGroupText.setVisibility(View.VISIBLE);
                        noTaskGroupIcon.setVisibility(View.VISIBLE);
                        taskGroupsRecyclerView.setVisibility(View.GONE);
                    } else {
                        noTaskGroupText.setVisibility(View.GONE);
                        noTaskGroupIcon.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load task groups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ProjectsActivity", "Error fetching task groups", e);
                });
    }



    private void refreshTaskGroups() {
        fetchTaskGroups(); // Refresh data by fetching from Firestore again
    }

    // Inner class for RecyclerView Adapter
    private class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskGroupViewHolder> {

        @NonNull
        @Override
        public TaskGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ProjectsActivity.this).inflate(R.layout.item_task_group_card, parent, false);
            return new TaskGroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskGroupViewHolder holder, int position) {
            Map<String, Object> taskGroup = taskGroups.get(position);

            holder.taskGroupName.setText(taskGroup.get("name") != null ? taskGroup.get("name").toString() : "Unnamed Group");

            String groupId = taskGroup.get("id") != null ? taskGroup.get("id").toString() : "";
            if (!groupId.isEmpty()) {
                db.collection("taskGroups")
                        .document(groupId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<Map<String, Object>> tasks = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                                if (tasks != null) {
                                    int totalTasks = tasks.size();
                                    int completedTasks = 0;

                                    // Counter for completed tasks
                                    for (Map<String, Object> task : tasks) {
                                        Boolean status = (Boolean) task.get("status");
                                        if (status != null && status) {
                                            completedTasks++;
                                        }
                                    }

                                    holder.taskCount.setText(completedTasks + " of " + totalTasks + " tasks completed");
                                } else {
                                    holder.taskCount.setText("No tasks");
                                }
                            } else {
                                holder.taskCount.setText("No tasks found");
                            }
                        })
                        .addOnFailureListener(e -> holder.taskCount.setText("Error loading tasks"));
            } else {
                holder.taskCount.setText("No tasks");
            }
        }



        @Override
        public int getItemCount() {
            return taskGroups.size();
        }

        // ViewHolder class
        class TaskGroupViewHolder extends RecyclerView.ViewHolder {
            TextView taskGroupName;
            TextView taskCount;

            public TaskGroupViewHolder(@NonNull View itemView) {
                super(itemView);
                taskGroupName = itemView.findViewById(R.id.taskGroupName);
                taskCount = itemView.findViewById(R.id.taskCount);

                // Set click listener on the entire item view
                itemView.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Get the clicked task group data
                        Map<String, Object> taskGroup = taskGroups.get(position);

                        // Extract the task group ID or any other data
                        String taskGroupId = taskGroup.get("id") != null ? taskGroup.get("id").toString() : "";
                        String taskGroupName = taskGroup.get("name") != null ? taskGroup.get("name").toString() : "Unnamed Group";

                        // Create an intent to start TodoActivity
                        Intent intent = new Intent(ProjectsActivity.this, TodoActivity.class);
                        intent.putExtra("TASK_GROUP_ID", taskGroupId); // Pass the task group ID
                        intent.putExtra("TASK_GROUP_NAME", taskGroupName); // Optionally pass the name

                        // Start the activity
                        startActivity(intent);
                    }
                });
            }
        }
    }
}

