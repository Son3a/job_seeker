package com.nsb.job_seeker.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "keyword")
public class KeyWord {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    public KeyWord(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
