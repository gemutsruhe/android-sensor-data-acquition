package com.iotproject.location;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SensorReceiver implements SensorEventListener2 {

    ArrayList<String> sensorNameList = new ArrayList<>(Arrays.asList("LSM6DSO Acceleration Sensor", "LSM6DSO Gyroscope Sensor", "TMD4910 Uncalibrated lux Sensor"));

    Map<String, List<String>> sensorDataType = new HashMap<String, List<String>>() {{
        put("LSM6DSO Acceleration Sensor", Arrays.asList("x","y","z"));
        put("LSM6DSO Gyroscope Sensor", Arrays.asList("x","y","z"));
        put("TMD4910 Uncalibrated lux Sensor", Arrays.asList("x"));
        put("MAGNETIC_FIELD", Arrays.asList("x","y","z"));
        put("Gravity Sensor", Arrays.asList("x","y","z"));
    }};

    Map<String, JSONArray> onDataMap;
    Map<String, JSONArray> offDataMap;
    private SensorManager sensorManager;
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
        onDataMap = initDataMap();
        offDataMap = initDataMap();

        registerSensor(accelerometerSensor);
        registerSensor(gyroscopeSensor);
        registerSensor(lightSensor);

        //Observable<SensorEvent> sensorObservable = createSensorObservable();

        // 주기적으로 서버에 데이터 업로드
        /*sensorObservable
                .observeOn(Schedulers.io())
                .subscribe(sensorEvent -> {
                    uploadDataToServer(sensorEvent);
                }, throwable -> {
                    throwable.printStackTrace();
        });*/
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
    }

    private void registerSensor(Sensor sensor) {
        if(sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
    /*private Observable<SensorEvent> createSensorObservable() {
        return Observable.create((ObservableOnSubscribe<SensorEvent>) emitter -> {
            SensorEventListener2 sensorEventListener = new SensorEventListener2() {
                @Override
                public void onFlushCompleted(Sensor sensor) {

                }

                @Override
                public void onSensorChanged(SensorEvent event) {

                    emitter.onNext(event);

                    uploadDataToServer(event);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Handle accuracy changes if needed
                }
            };
            //for(Sensor sensor : sensorList) registerSensor(sensor);
            registerSensor(accelerometerSensor);
            registerSensor(gyroscopeSensor);
            //registerSensor(gravitySensor);
            registerSensor(lightSensor);

            emitter.setCancellable(() -> {
                sensorManager.unregisterListener(this);
            });
        });
    }*/

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        JSONObject jsonObject = getSensorData(sensorEvent);
        onDataMap.get(sensorEvent.sensor.getName()).put(jsonObject);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private JSONObject getSensorData(SensorEvent sensorEvent) {
        JSONObject jsonObject = new JSONObject();

        long timestamp = sensorEvent.timestamp;
        String sensorName = sensorEvent.sensor.getName();
        float[] values = sensorEvent.values;

        try {
            jsonObject.put("timestamp", timestamp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < values.length; i++) {
            try {
                //Log.e("TEST", "sensorName : " + sensorName);
                jsonObject.put(sensorDataType.get(sensorName).get(i), String.valueOf(sensorEvent.values[i]));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonObject;
    }

    public void uploadData(){

        JSONObject dataObject = new JSONObject();

        for(int i = 0; i < sensorNameList.size(); i++) {
            try {
                dataObject.put(sensorNameList.get(i), offDataMap.get(sensorNameList.get(i)));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        offDataMap = initDataMap();

        uploadDataToServer(dataObject.toString());
    }

    private void uploadDataToServer(String data) {
        String serverUrl = "https://4a35-14-36-150-190.ngrok-free.app/";;

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(data, MediaType.get("application/json"));
        // 서버에 POST 요청을 생성
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            //String dataString = response.body().toString();
            //JSONObject dataObject = new JSONObject(dataString);
            //JSONArray data = dataJson.getJSONArray("")
            if (response.isSuccessful()) {
                Log.e("TEST", "Data uploaded to server");
                //Log.e("TEST", "Response : " + dataObject.toString());
            } else {
                Log.e("TEST", "Failed to upload data. Response code: " + response.code());
            }
            response.close();
        } catch (Exception e) {
            // 예외 처리
            Log.e("TEST", e.toString());
            e.printStackTrace();
        }
    }

    public void changeDataMap() {
        offDataMap = onDataMap;
        onDataMap = initDataMap();
    }

    private Map<String, JSONArray> initDataMap() {
        Map<String, JSONArray> dataMap = new HashMap<>();
        for(int i = 0; i < sensorNameList.size(); i++) {
            dataMap.put(sensorNameList.get(i), new JSONArray());
        }
        return dataMap;
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }
}
