package com.nsb.job_seeker.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {KeyWord.class}, version = 1)
public abstract class KeywordDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "keyword.db";
    private static KeywordDatabase instance;

    public static synchronized KeywordDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), KeywordDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract KeywordDAO keywordDAO();
}
