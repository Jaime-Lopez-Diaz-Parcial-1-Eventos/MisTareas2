package com.example.mistareas2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mistareas2.MainActivity;
import com.example.mistareas2.R;
import com.example.mistareas2.dataBase.SQLiteHelper;
import com.example.mistareas2.dataBase.dao.TaskDao;
import com.example.mistareas2.domain.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRegistrationActivity extends AppCompatActivity {

    private EditText etName, etDescription, etDate, etPriority, etCost;
    private Button btnRegister, btnCancel;
    private FirebaseFirestore firebaseDB;
    private TaskDao taskDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_registration);

        firebaseDB = FirebaseFirestore.getInstance();

        // Inicializar SQLite y el DAO
        SQLiteHelper sqliteHelper = SQLiteHelper.getInstance(this);
        taskDao = sqliteHelper.taskDao();
        executorService = Executors.newSingleThreadExecutor();

        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etDate = findViewById(R.id.et_date);
        etPriority = findViewById(R.id.et_priority);
        etCost = findViewById(R.id.et_cost);

        btnRegister = findViewById(R.id.btn_register);
        btnCancel = findViewById(R.id.btn_cancel);

        btnRegister.setOnClickListener(v -> registerTask());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void registerTask() {
        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String date = etDate.getText().toString();
        String priority = etPriority.getText().toString();
        String cost = etCost.getText().toString();

        if (name.isEmpty() || description.isEmpty() || date.isEmpty() || priority.isEmpty() || cost.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una nueva tarea con un ID único
        Task task = new Task(UUID.randomUUID().toString(), name, description, date, priority, Double.parseDouble(cost), false);

        // Guardar la tarea en Firebase
        firebaseDB.collection("tasks").document(task.getId()).set(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskRegistrationActivity.this, "Tarea registrada en Firebase", Toast.LENGTH_SHORT).show();

                    // Almacenar la tarea en SQLite en segundo plano
                    executorService.execute(() -> {
                        taskDao.addTask(task);
                        runOnUiThread(() -> {
                            Toast.makeText(TaskRegistrationActivity.this, "Tarea almacenada localmente", Toast.LENGTH_SHORT).show();
                            // Redirigir al MainActivity
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(TaskRegistrationActivity.this, "Error al registrar la tarea", Toast.LENGTH_SHORT).show());
    }
}
