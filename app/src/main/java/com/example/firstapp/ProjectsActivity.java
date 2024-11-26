package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

/**
 * Manages the display and interaction of task groups for the current user.
 */
public class ProjectsActivity extends AppCompatActivity {
    private RecyclerView taskGroupsRecyclerView;
    private List<Map<String, Object>> taskGroups;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView noTaskGroupText;
    private ImageView noTaskGroupIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initializeViews();
        fetchTaskGroups();
    }

    /**
     * Initializes views and sets up event listeners for buttons.
     */
    private void initializeViews() {
        taskGroupsRecyclerView = findViewById(R.id.taskGroupsRecyclerView);
        taskGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noTaskGroupText = findViewById(R.id.noTaskGroupMessage);
        noTaskGroupIcon = findViewById(R.id.noTaskGroupIcon);
        taskGroups = new ArrayList<>();
        taskGroupsRecyclerView.setAdapter(new TaskGroupAdapter(taskGroups, this));

        ImageButton newProjectBtn = findViewById(R.id.newprojectbtn);
        newProjectBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, AddNewGroupActivity.class));
        });

        ImageButton userButton = findViewById(R.id.user);
        userButton.setOnClickListener(view -> {
            startActivity(new Intent(this, UserPageActivity.class));
        });
    }

    /**
     * Fetches task groups associated with the current user and updates the RecyclerView.
     */
    private void fetchTaskGroups() {
        taskGroups.clear();
        String userId = auth.getCurrentUser().getUid();

        db.collection("taskGroups")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> taskGroup = document.getData();
                        taskGroup.put("id", document.getId());
                        enrichTaskGroupData(taskGroup, document);
                        taskGroups.add(taskGroup);
                    }
                    taskGroupsRecyclerView.getAdapter().notifyDataSetChanged();
                    updateNoTaskGroupMessage();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load task groups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ProjectsActivity", "Error fetching task groups", e);
                });
    }

    /**
     * Enriches task group data with the number of total and completed tasks.
     *
     * @param taskGroup The task group data.
     * @param document  The Firestore document snapshot.
     */
    private void enrichTaskGroupData(Map<String, Object> taskGroup, QueryDocumentSnapshot document) {
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) document.get("tasks");
        if (tasks != null) {
            taskGroup.put("totalTasks", tasks.size());
            taskGroup.put("completedTasks", (int) tasks.stream()
                    .filter(task -> Boolean.TRUE.equals(task.get("status")))
                    .count());
        } else {
            taskGroup.put("totalTasks", 0);
            taskGroup.put("completedTasks", 0);
        }
    }

    /**
     * Updates the visibility of the "No Task Groups" message.
     */
    private void updateNoTaskGroupMessage() {
        if (taskGroups.isEmpty()) {
            noTaskGroupText.setVisibility(View.VISIBLE);
            noTaskGroupIcon.setVisibility(View.VISIBLE);
            taskGroupsRecyclerView.setVisibility(View.GONE);
        } else {
            noTaskGroupText.setVisibility(View.GONE);
            noTaskGroupIcon.setVisibility(View.GONE);
        }
    }
}
