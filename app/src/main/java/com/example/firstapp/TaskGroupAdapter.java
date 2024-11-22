package com.example.firstapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    }

    @Override
    public int getItemCount() {
        return taskGroups.size();
    }

    static class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupTitle;

        public TaskGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.taskGroupName); // Assuming you have a TextView with ID groupTitle
        }
    }
}
