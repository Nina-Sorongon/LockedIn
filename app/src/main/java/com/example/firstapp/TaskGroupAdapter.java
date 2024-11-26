package com.example.firstapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying task groups in a RecyclerView.
 */
public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskGroupViewHolder> {
    private final List<Map<String, Object>> taskGroups;
    private final Context context;

    public TaskGroupAdapter(List<Map<String, Object>> taskGroups, Context context) {
        this.taskGroups = taskGroups;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_group_card, parent, false);
        return new TaskGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskGroupViewHolder holder, int position) {
        Map<String, Object> taskGroup = taskGroups.get(position);
        holder.bindData(taskGroup);
    }

    @Override
    public int getItemCount() {
        return taskGroups.size();
    }

    /**
     * ViewHolder class for task group items.
     */
    class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskGroupName;
        private final TextView taskCount;

        public TaskGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            taskGroupName = itemView.findViewById(R.id.taskGroupName);
            taskCount = itemView.findViewById(R.id.taskCount);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Map<String, Object> taskGroup = taskGroups.get(position);
                    Intent intent = new Intent(context, TodoActivity.class);
                    intent.putExtra("TASK_GROUP_ID", taskGroup.get("id").toString());
                    intent.putExtra("TASK_GROUP_NAME", taskGroup.get("name").toString());
                    context.startActivity(intent);
                }
            });
        }

        /**
         * Binds data to the ViewHolder elements.
         *
         * @param taskGroup The task group data to display.
         */
        public void bindData(Map<String, Object> taskGroup) {
            taskGroupName.setText(taskGroup.get("name") != null ? taskGroup.get("name").toString() : "Unnamed Group");
            int totalTasks = (int) taskGroup.getOrDefault("totalTasks", 0);
            int completedTasks = (int) taskGroup.getOrDefault("completedTasks", 0);
            taskCount.setText(completedTasks + " of " + totalTasks + " tasks completed");
        }
    }
}
