package com.iotproject.location;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
    Button thisTableButton;
    boolean thisTableButtonClicked;
    Handler handler = new Handler();
    Context context;
    SensorReceiver sensorReceiver;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        is213Button = findViewById(R.id.is213Button);
        thisTableButton = findViewById(R.id.thisTableButton);

        is213Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                is213ButtonClicked = !is213ButtonClicked;
                if(is213ButtonClicked) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }).start();
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
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                sensorReceiver.changeDataMap();
                                sensorReceiver.uploadData();
                            }
                        }
                    }).start();
                } else {
                    thisTableButton.setText("THIS TABLE");
                    sensorReceiver.stop();
                }

            }
        });
    }
}
