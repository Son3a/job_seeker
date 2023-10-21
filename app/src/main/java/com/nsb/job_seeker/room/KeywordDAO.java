package com.nsb.job_seeker.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface KeywordDAO {
    @Insert
    void insertKeyword(KeyWord keyWord);

    @Query("select * from keyword")
    List<KeyWord> getListKeyword();

    @Query("select * from keyword where name = :name")
    List<KeyWord> checkKeyWord(String name);

    @Delete
    void deleteKeyword(KeyWord keyWord);

    @Query("Delete from keyword")
    void deleteAll();
}
