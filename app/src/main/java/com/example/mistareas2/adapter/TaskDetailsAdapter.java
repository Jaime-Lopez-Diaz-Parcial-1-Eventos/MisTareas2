package com.example.mistareas2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mistareas2.R;
import com.example.mistareas2.domain.Task;

import java.util.List;

public class TaskDetailsAdapter extends RecyclerView.Adapter<TaskDetailsAdapter.TaskViewHolder> {

    private final List<Task> taskList;

    public TaskDetailsAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_detail, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvName.setText(task.getName());
        holder.tvDescription.setText(task.getDescription());
        holder.tvDate.setText(task.getDate());
        holder.tvPriority.setText(task.getPriority());
        holder.tvCost.setText(String.format("Coste: %.2f €", task.getCost()));
        holder.tvStatus.setText(task.isCompleted() ? "Completada" : "Pendiente");
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvDate, tvPriority, tvCost, tvStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_task_name);
            tvDescription = itemView.findViewById(R.id.tv_task_description);
            tvDate = itemView.findViewById(R.id.tv_task_date);
            tvPriority = itemView.findViewById(R.id.tv_task_priority);
            tvCost = itemView.findViewById(R.id.tv_task_cost);
            tvStatus = itemView.findViewById(R.id.tv_task_status);
        }
    }
}
