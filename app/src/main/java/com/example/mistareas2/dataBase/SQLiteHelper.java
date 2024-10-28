package com.example.mistareas2.dataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mistareas2.dataBase.dao.TaskDao;
import com.example.mistareas2.domain.Task;

@Database(entities = {Task.class}, version = 1)
public abstract class SQLiteHelper extends RoomDatabase {

    private static SQLiteHelper instance;

    public abstract TaskDao taskDao();

    public static synchronized SQLiteHelper getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            SQLiteHelper.class, "tasksDB")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
