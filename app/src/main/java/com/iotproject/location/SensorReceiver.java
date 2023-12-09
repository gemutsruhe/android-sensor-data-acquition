package com.iotproject.location;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SensorReceiver implements SensorEventListener2 {

    Map<String, List<String>> sensorDataType = new HashMap<>() {{
        put("ACCELEROMETER", Arrays.asList("x","y","z"));
        put("GYROSCOPE", Arrays.asList("x","y","z"));
        put("LIGHT", Arrays.asList("x","y","z"));
        put("MAGNETIC_FIELD", Arrays.asList("x","y","z"));
    }};

    private SensorManager sensorManager;

    ArrayList<Sensor> sensorList = new ArrayList<>();
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;
    private Sensor gravitySensor;
    private Sensor lightSensor;
    private Sensor linearAccelerationSensor;
    private Sensor magneticSensor;
    private Sensor stepCounterSensor;
    private Sensor gameRotationVectorSensor;

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    SensorReceiver(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        getSensors();
        Observable<SensorEvent> sensorObservable = createSensorObservable();

        // 주기적으로 서버에 데이터 업로드
        sensorObservable
                .observeOn(Schedulers.io())
                .subscribe(sensorEvent -> {
                    uploadDataToServer(sensorEvent);
                }, throwable -> {
                    throwable.printStackTrace();
        });
    }

    public void getSensors() {
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        gameRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if(gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void registerSensor(Sensor sensor) {
        if(sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
    private Observable<SensorEvent> createSensorObservable() {
        return Observable.create((ObservableOnSubscribe<SensorEvent>) emitter -> {
            /*sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, gameRotationVectorSensor, SensorManager.SENSOR_DELAY_FASTEST);*?
             */
            for(Sensor sensor : sensorList) registerSensor(sensor);
            registerSensor(gyroscopeSensor);
            registerSensor(accelerometerSensor);


            emitter.setCancellable(() -> {
                sensorManager.unregisterListener(this);
            });
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private String getSensorData(SensorEvent sensorEvent) {
        JSONObject jsonObject = new JSONObject();

        long timestamp = sensorEvent.timestamp;
        String sensorName = sensorEvent.sensor.getName();
        float[] values = sensorEvent.values;

        try {
            jsonObject.put("sensorName", sensorName);
            jsonObject.put("timestamp", timestamp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < values.length; i++) {
            jsonObject.put(sensorsensorNamevalues[i]);
        }
        return null;
    }

    private void uploadDataToServer(SensorEvent sensorEvent) {
        String serverUrl = "http://example.com/upload";;

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(sensorData, MediaType.get("application/json"));

        // 서버에 POST 요청을 생성
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.d("SensorUpload", "Data uploaded to server: " + sensorData);
            } else {
                Log.e("SensorUpload", "Failed to upload data. Response code: " + response.code());
            }

            response.close();
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
        }
    }
}
