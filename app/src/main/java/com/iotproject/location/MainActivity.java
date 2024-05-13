package com.iotproject.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    Button is213Button;

    boolean is213ButtonClicked;
    TextView isInDoorTextView;
    Button thisTableButton;
    boolean thisTableButtonClicked;

    Button getThisTableButton;
    Handler handler = new Handler();
    Context context;
    SensorReceiver sensorReceiver;
    EditText setTableLabel;

    LinearLayout tableAccuracyLinearLayout;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        is213Button = findViewById(R.id.is213Button);
        isInDoorTextView = findViewById(R.id.isInDoorTextView);
        thisTableButton = findViewById(R.id.thisTableButton);
        setTableLabel = findViewById(R.id.setTableLabel);
        getThisTableButton = findViewById(R.id.getThisTableButton);

        tableAccuracyLinearLayout = findViewById(R.id.tableAccuracyLinearLayout);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE)
                .check();

        is213Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                is213ButtonClicked = !is213ButtonClicked;
                if(is213ButtonClicked) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                            // WifiReceiver 등록
                            WifiReceiver wifiReceiver = new WifiReceiver();

                            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                            wifiManager.startScan();

                            new Thread(() -> {
                                while(wifiReceiver.isPost() == false) {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                //wifiReceiver.
                                isInDoorTextView.setText(wifiReceiver.getWhere());
                                unregisterReceiver(wifiReceiver);
                            }).start();
                        }
                    }).start();

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    is213ButtonClicked = !is213ButtonClicked;
                } else {
                    //sensorReceiver.pause();
                }
            }
        });
        thisTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisTableButtonClicked = !thisTableButtonClicked;
                if(thisTableButtonClicked) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sensorReceiver = new SensorReceiver(context);
                            thisTableButton.setText("running");
                            while(thisTableButtonClicked) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                sensorReceiver.changeDataMap();
                                sensorReceiver.uploadData(setTableLabel.getText().toString());
                            }
                        }
                    }).start();
                } else {
                    thisTableButton.setText("THIS TABLE");
                    sensorReceiver.stop();
                }

            }
        });
        getThisTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getThisTable();
                    }
                }).start();

            }
        });
    }

    private void getThisTable() {
        //String serverUrl = "http://10.210.62.57:3000/attend";
        //String serverUrl = "http://10.210.60.238:3000/attend";
        String serverUrl = "http://10.210.61.152:3000/attend";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("label", setTableLabel.getText().toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(serverUrl)
                .header("Content-Type", "application/json;utf-8")
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                Log.e("TEST", "Data uploaded to server");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tableAccuracyLinearLayout.removeAllViews();
                    }
                });
                ViewGroup parent = tableAccuracyLinearLayout;
                JSONObject responseJsonObject = new JSONObject(response.body().string());

                Iterator<String> keys = responseJsonObject.keys();
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                Map<String, Integer> tableCountMap = new HashMap<>();
                while(keys.hasNext()) {
                    String key = keys.next();
                    Log.e("TEST5", key);
                    JSONArray jsonArray = (JSONArray)responseJsonObject.get(key);

                    List<View> views = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONArray tableList = jsonArray.getJSONArray(i);
                        String tableNum = (String) tableList.get(0);
                        int count = (Integer) tableList.get(1);

                        View view = inflater.inflate(R.layout.table_accuracy_view, parent, false);
                        TextView tableNumTextView = view.findViewById(R.id.tableNumTextView);
                        tableNumTextView.setText(tableNum);
                        TextView tableCountTextView = view.findViewById(R.id.accuracyTextView);
                        tableCountTextView.setText(String.valueOf(count));

                        views.add(view);
                    }
                    runOnUiThread(() -> {
                        for (View view : views) {
                            parent.addView(view);
                        }
                    });
                }

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
}
