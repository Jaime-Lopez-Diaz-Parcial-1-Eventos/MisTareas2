package com.example.mistareas2.domain;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity(tableName = "tasks")
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @PrimaryKey
    @NonNull
    private String id;
    private String description;
    private boolean isCompleted;

    @NonNull
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
