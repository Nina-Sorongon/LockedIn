package com.example.firstapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskGroupViewHolder> {
    private final List<Map<String, Object>> taskGroups;
    private final Context context;

    public TaskGroupAdapter(Context context, List<Map<String, Object>> taskGroups) {
        this.context = context;
        this.taskGroups = taskGroups;
    }

    @NonNull
    @Override
    public TaskGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_group_card, parent, false);
        return new TaskGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskGroupViewHolder holder, int position) {
        Map<String, Object> taskGroup = taskGroups.get(position);
        String groupName = (String) taskGroup.get("name");

        holder.groupTitle.setText(groupName);

        // Handle click event to navigate to TodoActivity
        holder.groupTitle.setOnClickListener(v -> {
            Intent intent = new Intent(context, TodoActivity.class);
            intent.putExtra("groupId", (String) taskGroup.get("id")); // Pass group ID
            context.startActivity(intent);
        });

        // Handle delete button click
        holder.deleteBtn.setOnClickListener(v -> {
            String groupId = (String) taskGroup.get("id");
            deleteTaskGroup(groupId);
        });
    }

    @Override
    public int getItemCount() {
        return taskGroups.size();
    }

    // Method to delete task group from Firestore
    private void deleteTaskGroup(String groupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("taskGroups").document(groupId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the task group from the list and notify the adapter
                    for (Map<String, Object> group : taskGroups) {
                        if (group.get("id").equals(groupId)) {
                            taskGroups.remove(group);
                            break;
                        }
                    }
                    notifyDataSetChanged();  // Update the RecyclerView
                    Toast.makeText(context, "Task Group deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete task group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    static class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupTitle;
        ImageButton deleteBtn;

        public TaskGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.taskGroupName);
            deleteBtn = itemView.findViewById(R.id.deleteTaskGroupBtn);
        }
    }
}

