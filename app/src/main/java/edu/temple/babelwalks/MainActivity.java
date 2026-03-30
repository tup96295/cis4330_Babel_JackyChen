package edu.temple.babelwalks;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, stepSensor;

    private float accelMagnitude = 0;
    private float gyroMagnitude = 0;
    private int stepCount = 0;

    private long startTime;

    private TextView accelView, gyroView, stepView, speedView, resultView;
    private EditText distanceInput;
    private Button calculateBtn;

    private MLModel mlModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelView = findViewById(R.id.accelView);
        gyroView = findViewById(R.id.gyroView);
        stepView = findViewById(R.id.stepView);
        speedView = findViewById(R.id.speedView);
        resultView = findViewById(R.id.resultView);
        distanceInput = findViewById(R.id.distanceInput);
        calculateBtn = findViewById(R.id.calculateBtn);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mlModel = new MLModel(getAssets());

        startTime = System.currentTimeMillis();

        calculateBtn.setOnClickListener(v -> calculateTime());
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            accelMagnitude = (float) Math.sqrt(x * x + y * y + z * z);
            accelView.setText("Accel: " + accelMagnitude);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            gyroMagnitude = Math.abs(x) + Math.abs(y) + Math.abs(z);
            gyroView.setText("Gyro: " + gyroMagnitude);
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++;
            stepView.setText("Steps: " + stepCount);
        }

        updateSpeed();
    }

    private void updateSpeed() {
        long currentTime = System.currentTimeMillis();
        float elapsedSeconds = (currentTime - startTime) / 1000f;

        float[] input = new float[]{
                accelMagnitude,
                gyroMagnitude,
                stepCount,
                elapsedSeconds
        };

        float speed = mlModel.predict(input);
        speedView.setText("Speed: " + speed + " m/s");
    }

    private void calculateTime() {
        String inputStr = distanceInput.getText().toString();
        if (inputStr.isEmpty()) return;

        float distance = Float.parseFloat(inputStr);

        long currentTime = System.currentTimeMillis();
        float elapsedSeconds = (currentTime - startTime) / 1000f;

        float[] input = new float[]{
                accelMagnitude,
                gyroMagnitude,
                stepCount,
                elapsedSeconds
        };

        float speed = mlModel.predict(input);

        if (speed <= 0) return;

        float time = distance / speed;

        resultView.setText("Estimated Time: " + time + " seconds");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
