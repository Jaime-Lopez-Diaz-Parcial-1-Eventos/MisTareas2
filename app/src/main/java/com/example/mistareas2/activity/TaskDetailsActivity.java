package com.example.mistareas2.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mistareas2.R;
import com.example.mistareas2.adapter.TaskDetailsAdapter;
import com.example.mistareas2.domain.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseDB;
    private List<Task> taskList;
    private TaskDetailsAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        firebaseDB = FirebaseFirestore.getInstance();
        taskList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskDetailsAdapter(taskList);
        recyclerView.setAdapter(taskAdapter);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        loadAllTasks();
    }

    private void loadAllTasks() {
        firebaseDB.collection("tasks").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            taskList.add(t);
                        }
                        taskAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TaskDetailsActivity.this, "Error al cargar las tareas", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
