package edu.temple.babelwalks;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SensorData {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public float accel;
    public float gyro;
    public int steps;
    public float speed;
    public long timestamp;
}