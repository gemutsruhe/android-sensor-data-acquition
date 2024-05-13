package com.iotproject.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WifiReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    String where;
    boolean post = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("TEST3", "permission");
            return;
        }
        post = false;
        wifiReceiver = this;
        List<ScanResult> scanResults = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getScanResults();
        //List<ScanResult> scanResults = wifiManager.getScanResults();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("label", "wifi");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // 결과를 ListView에 추가
        for (ScanResult scanResult : scanResults) {
            //adapter.add(scanResult.SSID); // SSID는 Wi-Fi 네트워크의 이름
            if(scanResult.SSID.startsWith("Smart-CAU_2.4G")) {
                try {
                    jsonObject.put(scanResult.BSSID, scanResult.level);
                    if(scanResult.BSSID.compareTo("a8:5b:f7:30:14:e1") == 0) {
                        if(scanResult.level > -58) {
                            where = "Indoor";
                            //Log.e("TEST","Indoor");
                        } else {
                            where = "Outdoor";
                            Log.e("TEST","Outdoor");
                        }
                        post = true;
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Log.e("TEST1", scanResult.SSID + "     " + scanResult.BSSID + "      " + scanResult.level);
            }

        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //uploadDataToServer(jsonObject.toString());

            }
        }).start();
    }

    private void uploadDataToServer(String data) {
        //String serverUrl = "http://ec2-18-221-192-14.us-east-2.compute.amazonaws.com:3000";
        String serverUrl = "http://10.210.62.57:3000";

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(data, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(serverUrl)
                .header("Content-Type", "application/json;utf-8")
                .post(requestBody)
                .build();
        try {
            Log.e("TEST", String.valueOf(request.body().contentLength()));
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.e("TEST", "Data uploaded to server");
                //Log.e("TEST", "Response : " + dataObject.toString());
            } else {
                Log.e("TEST", "Failed to upload data. Response code: " + response.code());
            }
            post = true;
            JSONObject responseJsonObject = new JSONObject(response.body().string());
            Log.e("TEST", responseJsonObject.toString());
            response.close();
        } catch (Exception e) {
            // 예외 처리
            Log.e("TEST", e.toString());
            e.printStackTrace();
        }
    }

    public boolean isPost() {
        return post;
    }

    public String getWhere() {
        return where;
    }
}
