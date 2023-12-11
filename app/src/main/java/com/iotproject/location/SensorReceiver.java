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
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SensorReceiver implements SensorEventListener2 {

    Map<String, List<String>> sensorDataType = new HashMap<String, List<String>>() {{
        put("LSM6DSO Acceleration Sensor", Arrays.asList("x","y","z"));
        put("LSM6DSO Gyroscope Sensor", Arrays.asList("x","y","z"));
        put("TMD4910 Uncalibrated lux Sensor", Arrays.asList("x","y","z"));
        put("MAGNETIC_FIELD", Arrays.asList("x","y","z"));
        put("Gravity Sensor", Arrays.asList("x","y","z"));
    }};

    Map<String, JSONArray> dataMap = new HashMap<String, JSONArray>() {{
        put("LSM6DSO Acceleration Sensor", new JSONArray());
        put("LSM6DSO Gyroscope Sensor", new JSONArray());
        put("TMD4910 Uncalibrated lux Sensor", new JSONArray());
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
    int count = 0;
    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    SensorReceiver(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        getSensors();

        //registerSensor(gyroscopeSensor);
        //registerSensor(accelerometerSensor);
        //registerSensor(gravitySensor);
        //registerSensor(lightSensor);
        //registerSensor(linearAccelerationSensor);

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
    }

    private void registerSensor(Sensor sensor) {
        if(sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.e("TEST", sensor.getName());
        }
    }
    private Observable<SensorEvent> createSensorObservable() {
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
            /*sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, gameRotationVectorSensor, SensorManager.SENSOR_DELAY_FASTEST);*?
             */
            //for(Sensor sensor : sensorList) registerSensor(sensor);
            //registerSensor(gyroscopeSensor);
            //registerSensor(accelerometerSensor);
            //registerSensor(gravitySensor);
            registerSensor(lightSensor);

            emitter.setCancellable(() -> {
                sensorManager.unregisterListener(this);
            });
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //sensorEvent.sensor.getName();

        //Log.e("TEST", sensorEvent.sensor.getName() + " : " + sensorEvent.sensor.toString());
        String value = "";
        for(int i = 0; i < sensorEvent.values.length; i++)  value += sensorEvent.values[i] + "    ";
        //Log.e("TEST", value);
        JSONObject jsonObject = getSensorData(sensorEvent);
        dataMap.get(sensorEvent.sensor.getName()).put(jsonObject);
        Log.e("TEST", jsonObject.toString());
        if(dataMap.get(sensorEvent.sensor.getName()).length() % 100 == 0) Log.e("TEST", String.valueOf(dataMap.get(sensorEvent.sensor.getName()).length()));
        if(sensorEvent.sensor.getName() == "LSM6DSO Gyroscope Sensor") {

            dataMap.get(sensorEvent.sensor.getName()).put(getSensorData(sensorEvent));

            if(count == 99) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadDataToServer(sensorEvent);

                    }
                }).start();
            }
            count++;
            count %= 100;
        }



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
            jsonObject.put("sensorName", sensorName);
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

    private void uploadDataToServer(SensorEvent sensorEvent) {
        String serverUrl = "https://4a35-14-36-150-190.ngrok-free.app/";;

        //String sensorData = getSensorData(sensorEvent);
        JSONArray dataJsonArray = dataMap.get(sensorEvent.sensor.getName());
        Log.e("TEST", String.valueOf(dataJsonArray.length()));
        String sensorData = dataMap.get(sensorEvent.sensor.getName()).toString();
        //dataMap.get(sensorEvent.sensor.getName().toString()).new JSONArray();
        Log.e("TEST", sensorData);
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(sensorData, MediaType.get("application/json"));
        // 서버에 POST 요청을 생성
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();



        try {
            Response response = client.newCall(request).execute();
            String dataString = response.body().toString();
            JSONObject dataJson = new JSONObject(dataString);
            //JSONArray data = dataJson.getJSONArray("")
            if (response.isSuccessful()) {
                Log.e("TEST", "Data uploaded to server: " + sensorData);
                Log.e("TEST", "Response : " + dataJson.toString());
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

    public void pause(){
        //sensorManager.unregisterListener(this);
        sensorManager.unregisterListener(this, lightSensor);
        for(int i = 0; i < dataMap.get("TMD4910 Uncalibrated lux Sensor").length(); i++) {
            try {
                Log.e("TEST", dataMap.get("TMD4910 Uncalibrated lux Sensor").get(i).toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        //uploadDataToServer();
    }

}
