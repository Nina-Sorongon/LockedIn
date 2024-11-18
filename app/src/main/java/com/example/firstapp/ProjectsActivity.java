package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

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
    private TaskGroupAdapter adapter;
    private List<Map<String, Object>> taskGroups;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> addGroupLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        taskGroupsRecyclerView = findViewById(R.id.taskGroupsRecyclerView);
        taskGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskGroups = new ArrayList<>();
        adapter = new TaskGroupAdapter(this, taskGroups);
        taskGroupsRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Initialize ActivityResultLauncher
        addGroupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshTaskGroups();
                    }
                }
        );

        fetchTaskGroups();

        ImageButton newprojectBtn = findViewById(R.id.newprojectbtn);
        newprojectBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddNewGroupActivity.class);
            addGroupLauncher.launch(intent);
        });
    }

    private void fetchTaskGroups() {
        taskGroups.clear();

        // Get the currently authenticated user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Query Firestore to get task groups for this user
        db.collection("taskGroups")
                .whereEqualTo("userId", userId) // Filter by userId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        taskGroups.add(document.getData());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load task groups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshTaskGroups() {
        fetchTaskGroups(); // Re-fetch data from Firestore
    }
}
