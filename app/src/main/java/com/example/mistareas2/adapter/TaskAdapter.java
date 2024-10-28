package com.example.mistareas2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mistareas2.R;
import com.example.mistareas2.domain.Task;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {

    private final TaskActionListener listener;

    public interface TaskActionListener {
        void onDeleteTask(Task task);
        void onMarkTaskAsDone(Task task);
    }

    public TaskAdapter(@NonNull Context context, @NonNull List<Task> tasks, TaskActionListener listener) {
        super(context, 0, tasks);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Task task = getItem(position);

        // Seleccionar el layout según el estado de la tarea
        int layoutId = (task != null && task.isCompleted()) ?
                R.layout.task_item_completed : R.layout.task_item_pending;

        if (convertView == null || !convertView.getTag().equals(layoutId)) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            convertView.setTag(layoutId); // Almacenar el ID de layout como etiqueta
        }

        if (task != null) {
            TextView tvTaskDescription = convertView.findViewById(R.id.tv_task_description);
            Button btnDeleteTask = convertView.findViewById(R.id.btn_delete_task);
            Button btnMarkDone = convertView.findViewById(R.id.btn_mark_done);

            tvTaskDescription.setText(task.getDescription());
            btnDeleteTask.setText(getContext().getString(R.string.eliminar_tarea));

            // Para el botón de completado o marcado como hecho
            if (task.isCompleted()) {
                btnMarkDone.setText(getContext().getString(R.string.completada));
            } else {
                btnMarkDone.setText(getContext().getString(R.string.marcar_como_hecha));
            }

            btnDeleteTask.setOnClickListener(v -> listener.onDeleteTask(task));
            btnMarkDone.setOnClickListener(v -> listener.onMarkTaskAsDone(task));
        }

        return convertView;
    }
}

