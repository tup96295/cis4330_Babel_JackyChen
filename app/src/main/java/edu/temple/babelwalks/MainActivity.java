package edu.temple.babelwalks;

import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, stepSensor;

    private float accelMagnitude = 0;
    private float gyroMagnitude = 0;
    private int stepCount = 0;

    private long startTime;

    private TextView accelView, gyroView, stepView, speedView, resultView;
    private Button calculateBtn;

    private GoogleMap mMap;
    private Marker startMarker, destinationMarker;
    private Polyline routePolyline;

    private LatLng startLatLng, destLatLng;
    private AppDB db;

    private static final String API_KEY = "AIzaSyCitZJRKVo0c8aL4TzOpymjTYCNwkPMW5U"; //unrestricted key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelView = findViewById(R.id.accelView);
        gyroView = findViewById(R.id.gyroView);
        stepView = findViewById(R.id.stepView);
        speedView = findViewById(R.id.speedView);
        resultView = findViewById(R.id.resultView);
        calculateBtn = findViewById(R.id.calculateBtn);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        startTime = System.currentTimeMillis();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyA8Wd6KGyBXMvrq00PFX9fiLUpnfQaUifU");//Map Api_key
        }

        AutocompleteSupportFragment startFrag =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.start_autocomplete);

        AutocompleteSupportFragment destFrag =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.dest_autocomplete);

        startFrag.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));
        destFrag.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));

        startFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                startLatLng = place.getLatLng();
                if (startMarker != null) startMarker.remove();
                startMarker = mMap.addMarker(new MarkerOptions().position(startLatLng).title(place.getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 12));
            }

            @Override
            public void onError(Status status) {
                resultView.setText("Start error: " + status.getStatusMessage());
            }
        });

        destFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destLatLng = place.getLatLng();
                if (destinationMarker != null) destinationMarker.remove();
                destinationMarker = mMap.addMarker(new MarkerOptions().position(destLatLng).title(place.getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLatLng, 12));
            }

            @Override
            public void onError(Status status) {
                resultView.setText("Dest error: " + status.getStatusMessage());
            }
        });

        calculateBtn.setOnClickListener(v -> fetchRoute());

        db = androidx.room.Room.databaseBuilder(
                getApplicationContext(),
                AppDB.class,
                "sensor-db"
        ).allowMainThreadQueries().build();
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
        SensorData data = new SensorData();
        data.accel = accelMagnitude;
        data.gyro = gyroMagnitude;
        data.steps = stepCount;
        data.speed = 0.90f;
        data.timestamp = System.currentTimeMillis();

        db.sensorDataDao().insert(data);
    }

    private void updateSpeed() {
        float speed = 0.90f;
        speedView.setText("Speed: " + speed + " m/s");
    }
    //private void updateSpeed() {
        //long currentTime = System.currentTimeMillis();
        //float elapsedSeconds = (currentTime - startTime) / 1000f;

        //if (elapsedSeconds == 0) return;

        //float stepsPerSecond = stepCount / elapsedSeconds;
        //float strideLength = 0.75f;

        //float speed = stepsPerSecond * strideLength;

        //speedView.setText("Speed: " + speed + " m/s");
    //}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void fetchRoute() {

        if (startLatLng == null || destLatLng == null) {
            resultView.setText("Select both locations");
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + startLatLng.latitude + "," + startLatLng.longitude
                + "&destination=" + destLatLng.latitude + "," + destLatLng.longitude
                + "&mode=walking&key=" + API_KEY;

        new Thread(() -> {
            try {
                String json = downloadUrl(url);
                runOnUiThread(() -> parseRoute(json));
            } catch (Exception e) {
                runOnUiThread(() -> resultView.setText("Network error"));
            }
        }).start();
    }

    private String downloadUrl(String strUrl) throws Exception {
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        InputStream is = conn.getInputStream();
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private void parseRoute(String json) {
        try {
            JSONObject data = new JSONObject(json);

            String status = data.getString("status");

            if (!status.equals("OK")) {
                resultView.setText("API Error: " + status);
                return;
            }

            JSONArray routes = data.getJSONArray("routes");

            if (routes.length() == 0) {
                resultView.setText("No route found");
                return;
            }

            JSONObject route = routes.getJSONObject(0);
            JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

            double distanceMeters = leg.getJSONObject("distance").getDouble("value");

            String polyline = route.getJSONObject("overview_polyline").getString("points");

            drawPolyline(polyline);
            calculateTime(distanceMeters);

        } catch (Exception e) {
            resultView.setText("Route error");
        }
    }

    private void drawPolyline(String encoded) {
        if (routePolyline != null) routePolyline.remove();
        PolylineOptions options = new PolylineOptions().addAll(decodePolyline(encoded)).width(10);
        routePolyline = mMap.addPolyline(options);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lat += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lng += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }

    private void calculateTime(double distanceMeters) {

        float distanceMiles = (float) (distanceMeters * 0.000621371);

        float speed = 0.90f; //remove this and uncomment the following code for sensor data

        //long currentTime = System.currentTimeMillis();
        //float elapsedSeconds = (currentTime - startTime) / 1000f;

        //if (elapsedSeconds == 0) return;

        //float stepsPerSecond = stepCount / elapsedSeconds;
        //float strideLength = 0.75f;

        //float speed = stepsPerSecond * strideLength;

        float timeMinutes = (float) ((distanceMeters / speed) / 60);

        resultView.setText(
                "Distance: " + distanceMiles + " miles\n" +
                        "Time: " + timeMinutes + " minutes"
        );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}