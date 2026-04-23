package edu.temple.babelwalks;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SensorData.class}, version = 1)
public abstract class AppDB extends RoomDatabase {
    public abstract SensorDataDao sensorDataDao();
}