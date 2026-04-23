package edu.temple.babelwalks;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SensorDataDao {

    @Insert
    void insert(SensorData data);

    @Query("SELECT * FROM SensorData")
    List<SensorData> getAll();
}