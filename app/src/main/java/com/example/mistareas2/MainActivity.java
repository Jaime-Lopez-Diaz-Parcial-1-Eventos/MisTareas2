package com.example.mistareas2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.mistareas2.activity.SettingsActivity;
import com.example.mistareas2.adapter.TaskAdapter;
import com.example.mistareas2.dataBase.SQLiteHelper;
import com.example.mistareas2.dataBase.dao.TaskDao;
import com.example.mistareas2.domain.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseDB;
    private SQLiteHelper sqliteHelper;
    private TaskDao taskDao;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private boolean showingPendingTasks = true;
    private EditText etNewTask;
    private Button btnAddTask;
    private Button btnPendingTasks;
    private Button btnCompletedTasks;
    private Button btnSettings;
    private ListView taskListView;

    // Añadimos referencias al DrawerLayout y Toolbar
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    // ExecutorService para manejar la concurrencia
    private ExecutorService executorService;

    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadThemePreference(); // Cargar el tema antes de super.onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el ExecutorService
        executorService = Executors.newFixedThreadPool(4);

        // Inicializar Firebase y SQLite
        firebaseDB = FirebaseFirestore.getInstance();
        sqliteHelper = SQLiteHelper.getInstance(this);
        taskDao = sqliteHelper.taskDao();

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar_main);
        etNewTask = findViewById(R.id.et_new_task);
        btnAddTask = findViewById(R.id.btn_add_task);
        btnPendingTasks = findViewById(R.id.btn_pending_tasks);
        btnCompletedTasks = findViewById(R.id.btn_completed_tasks);
        btnSettings = findViewById(R.id.btn_settings);
        taskListView = findViewById(R.id.task_list_view);

        // Configurar el Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu); // Icono del menú
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Inicializar lista de tareas y adaptador
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, taskList, new TaskAdapter.TaskActionListener() {
            @Override
            public void onDeleteTask(Task task) {
                deleteTask(task);
            }

            @Override
            public void onMarkTaskAsDone(Task task) {
                markTaskAsDone(task);
            }
        });
        taskListView.setAdapter(taskAdapter);

        // Establecer listeners
        btnAddTask.setOnClickListener(v -> addNewTask());
        btnPendingTasks.setOnClickListener(v -> {
            showingPendingTasks = true;
            loadTasks();
        });
        btnCompletedTasks.setOnClickListener(v -> {
            showingPendingTasks = false;
            loadTasks();
        });
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Cargar tareas al inicio
        loadTasks();
    }

    private void loadThemePreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        setAppTheme(isDarkMode);
    }

    private void setAppTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar el título de la Toolbar para reflejar el cambio de idioma
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
        }
    }


    private void addNewTask() {
        String description = etNewTask.getText().toString().trim();
        if (!description.isEmpty()) {
            String id = UUID.randomUUID().toString();
            Task task = new Task(id, description, false);

            // Agregar a Firebase
            firebaseDB.collection("tasks").document(id).set(task)
                    .addOnSuccessListener(aVoid -> {
                        // Agregar a SQLite usando ExecutorService
                        executorService.execute(() -> {
                            taskDao.addTask(task);
                            runOnUiThread(() -> {
                                etNewTask.setText("");
                                loadTasks();
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Error al agregar tarea en Firebase", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadTasks() {
        executorService.execute(() -> {
            List<Task> tasksFromSQLite = taskDao.getTasks(!showingPendingTasks);
            if (tasksFromSQLite != null && !tasksFromSQLite.isEmpty()) {
                runOnUiThread(() -> {
                    taskList.clear();
                    taskList.addAll(tasksFromSQLite);
                    taskAdapter.notifyDataSetChanged();
                });
            } else {
                // Si no hay datos en SQLite, cargar desde Firebase y guardar en SQLite
                firebaseDB.collection("tasks")
                        .whereEqualTo("completed", !showingPendingTasks)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                taskList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Task t = document.toObject(Task.class);
                                    taskList.add(t);
                                    // Guardar en SQLite
                                    executorService.execute(() -> taskDao.addTask(t));
                                }
                                runOnUiThread(taskAdapter::notifyDataSetChanged);
                            } else {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al cargar tareas desde Firebase", Toast.LENGTH_SHORT).show());
                            }
                        });
            }
        });
    }

    private void deleteTask(Task task) {
        firebaseDB.collection("tasks").document(task.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    executorService.execute(() -> {
                        taskDao.deleteTask(task);
                        runOnUiThread(this::loadTasks);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error al eliminar tarea en Firebase", Toast.LENGTH_SHORT).show());
    }

    private void markTaskAsDone(Task task) {
        task.setCompleted(true);
        firebaseDB.collection("tasks").document(task.getId()).set(task)
                .addOnSuccessListener(aVoid -> {
                    executorService.execute(() -> {
                        taskDao.updateTask(task);
                        runOnUiThread(this::loadTasks);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error al actualizar tarea en Firebase", Toast.LENGTH_SHORT).show());
    }

    // Manejar el botón de retroceso para cerrar el menú si está abierto
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Actualizar el contexto para aplicar el idioma seleccionado
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sharedPreferences = newBase.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String language = sharedPreferences.getString(KEY_LANGUAGE, "es");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }
}
