package com.example.firstapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddNewGroupActivity extends AppCompatActivity {
    private EditText groupNameInput;
    private Spinner imageSpinner;
    private Button createGroupButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewgroup);

        groupNameInput = findViewById(R.id.edittitle);
        imageSpinner = findViewById(R.id.imageSpinner);
        createGroupButton = findViewById(R.id.creategroupbtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();




        createGroupButton.setOnClickListener(view -> createTaskGroup());
    }

    private void createTaskGroup() {
        String groupName = groupNameInput.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(this, "Group name cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Prepare task group data
        Map<String, Object> taskGroup = new HashMap<>();
        taskGroup.put("name", groupName);
        taskGroup.put("userId", userId);
        taskGroup.put("email", user.getEmail());
        taskGroup.put("tasks", null); // No tasks initially

        // Add task group to Firestore
        db.collection("taskGroups")
                .add(taskGroup)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task group created!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating task group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
